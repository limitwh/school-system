package com.school.service.impl;

import com.school.common.BusinessException;
import com.school.common.UserStatus;
import com.school.dto.ChangePasswordRequest;
import com.school.dto.LoginRequest;
import com.school.dto.LoginResponse;
import com.school.dto.SetPasswordRequest;
import com.school.entity.SysRole;
import com.school.entity.SysUser;
import com.school.repository.SysUserRepository;
import com.school.security.JwtUtil;
import com.school.security.LoginUser;
import com.school.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    /** 密码连续失败最大次数，达到后锁定 */
    private static final int MAX_FAIL_COUNT = 4;

    /** 密码有效期（天）：修改密码后 N 天内有效，超过则状态置为「密码过期」 */
    private static final long PASSWORD_VALID_DAYS = 90;

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SysUserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsernameOrLoginNo(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));

        Integer status = user.getStatus() == null ? UserStatus.NORMAL : user.getStatus();

        // 锁定状态：直接拒绝登录
        if (status == UserStatus.LOCKED) {
            throw new BusinessException(423, "密码错误次数过多，账号已锁定，请联系管理员");
        }

        // 密码过期检测：当前时间晚于密码到期时间 → 状态置为「密码过期」
        if (isPasswordExpired(user)) {
            if (status != UserStatus.PASSWORD_EXPIRED) {
                user.setStatus(UserStatus.PASSWORD_EXPIRED);
                userRepository.save(user);
                status = UserStatus.PASSWORD_EXPIRED;
            }
        }

        // 状态校验：仅「初始(0)」「正常(1)」「密码过期(10)」可登录，其余状态拒绝
        if (status != UserStatus.INITIAL
                && status != UserStatus.NORMAL
                && status != UserStatus.PASSWORD_EXPIRED) {
            throw new BusinessException(403, "账号状态异常，无法登录");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int newCount = (user.getPwdFailCount() == null ? 0 : user.getPwdFailCount()) + 1;
            user.setPwdFailCount(newCount);
            if (newCount >= MAX_FAIL_COUNT) {
                // 连续失败达到阈值：锁定账号（状态置为锁定）
                user.setStatus(UserStatus.LOCKED);
                userRepository.save(user);
                throw new BusinessException(423, "密码错误次数过多，账号已锁定，请联系管理员");
            }
            userRepository.save(user);
            throw new BusinessException(401, "账号或密码错误");
        }

        // 登录成功：重置失败次数、更新最近登录时间
        user.setPwdFailCount(0);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 签发 JWT
        LoginUser loginUser = new LoginUser(user);
        String token = jwtUtil.generateToken(loginUser);

        List<String> roles = user.getRoles().stream().map(SysRole::getCode).toList();
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getStatus(), roles);
    }

    @Override
    @Transactional
    public void setPassword(LoginUser loginUser, SetPasswordRequest request) {
        SysUser user = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 仅初始状态用户需要且允许设置密码
        if (user.getStatus() == null || user.getStatus() != UserStatus.INITIAL) {
            throw new BusinessException("当前账号无需设置初始密码");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(UserStatus.NORMAL);
        user.setPwdFailCount(0);
        // 记录密码到期时间：修改时间 + 90 天
        user.setPwdUpdateTime(LocalDateTime.now().plusDays(PASSWORD_VALID_DAYS));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(LoginUser loginUser, ChangePasswordRequest request) {
        SysUser user = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 初始状态用户应通过「设置初始密码」完成，不允许在此修改
        if (user.getStatus() != null && user.getStatus() == UserStatus.INITIAL) {
            throw new BusinessException("请先设置初始密码");
        }

        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "原密码不正确");
        }

        // 更新密码、记录到期时间（修改时间 + 90 天）、状态置为正常、清除失败计数
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPwdUpdateTime(LocalDateTime.now().plusDays(PASSWORD_VALID_DAYS));
        user.setStatus(UserStatus.NORMAL);
        user.setPwdFailCount(0);
        userRepository.save(user);
    }

    /**
     * 判断密码是否已过期：pwd_update_time 存储到期时间，当前时间晚于到期时间即过期
     */
    private boolean isPasswordExpired(SysUser user) {
        return user.getPwdUpdateTime() != null
                && LocalDateTime.now().isAfter(user.getPwdUpdateTime());
    }
}
