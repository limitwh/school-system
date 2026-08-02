package com.school.service.impl;

import com.school.common.BusinessException;
import com.school.dto.LoginRequest;
import com.school.dto.LoginResponse;
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
    private static final int MAX_FAIL_COUNT = 5;

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
    @Transactional
    public LoginResponse login(LoginRequest request) {
        SysUser user = userRepository.findByUsernameOrLoginNo(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "账号或密码错误"));

        // 状态校验：仅「正常(1)」可登录
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "账号状态异常，无法登录");
        }

        // 密码失败次数锁定检查
        int failCount = user.getPwdFailCount() == null ? 0 : user.getPwdFailCount();
        if (failCount >= MAX_FAIL_COUNT) {
            throw new BusinessException(423, "密码错误次数过多，账号已锁定，请联系管理员");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setPwdFailCount(failCount + 1);
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
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), roles);
    }
}
