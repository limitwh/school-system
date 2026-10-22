package com.school.service.impl;

import com.school.common.BusinessException;
import com.school.common.UserStatus;
import com.school.dto.AdminUserResponse;
import com.school.dto.ResetPasswordRequest;
import com.school.dto.ResetPasswordResult;
import com.school.entity.SysUser;
import com.school.repository.SysUserRepository;
import com.school.service.AdminUserService;
import com.school.service.OperationLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管理员用户管理服务实现
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    /** 一次性密码字符集：去掉 0/O、1/l/I 等易混淆字符，便于口头传达 */
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String ALL_CHARS = UPPER + LOWER + DIGITS;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 一次性密码长度，可通过 app.password.reset-length 配置（最小 8） */
    @Value("${app.password.reset-length:10}")
    private int resetPasswordLength;

    /** 一次性密码有效期（小时），可通过 app.password.reset-valid-hours 配置 */
    @Value("${app.password.reset-valid-hours:24}")
    private long resetValidHours;

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public AdminUserServiceImpl(SysUserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                OperationLogService operationLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listLockedUsers() {
        return userRepository.findByStatus(UserStatus.LOCKED).stream()
                .map(u -> new AdminUserResponse(
                        u.getId(),
                        u.getUsername(),
                        u.getLoginNo(),
                        u.getRealName(),
                        u.getStatus(),
                        u.getLastLoginTime()))
                .toList();
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 仅允许重置「锁定」状态的用户
        if (user.getStatus() == null || user.getStatus() != UserStatus.LOCKED) {
            throw new BusinessException("仅可重置锁定状态的用户");
        }

        // 重置：分配初始密码，状态置为「初始(0)」，清除失败计数、密码到期时间与一次性密码有效期
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(UserStatus.INITIAL);
        user.setPwdFailCount(0);
        user.setPwdUpdateTime(null);
        user.setOtpExpireTime(null);
        userRepository.save(user);

        operationLogService.logCurrent("USER", "RESET_PASSWORD",
                "重置锁定用户密码：" + user.getUsername(), true);
    }

    @Override
    @Transactional
    public ResetPasswordResult resetPasswordByLoginName(Long operatorId, String loginName) {
        String trimmed = loginName == null ? "" : loginName.trim();
        SysUser user = userRepository.findByUsernameOrLoginNo(trimmed, trimmed)
                .orElseThrow(() -> new BusinessException(400, "用户不存在：" + trimmed));

        // 防止管理员误把自己重置为初始状态
        if (operatorId != null && operatorId.equals(user.getId())) {
            throw new BusinessException(400, "不能重置当前登录账号的密码");
        }

        String oneTimePassword = generateOneTimePassword();
        applyReset(user, oneTimePassword);

        operationLogService.logCurrent("USER", "RESET_PASSWORD_BY_NAME",
                "按登录名重置用户密码：" + user.getUsername() + "（一次性密码，" + resetValidHours + " 小时有效）",
                true);

        return new ResetPasswordResult(user.getUsername(), user.getLoginNo(),
                user.getRealName(), oneTimePassword, resetValidHours);
    }

    /**
     * 重置密码：状态回到「初始(0)」，用户下次登录需设置新密码；
     * 同时清除失败计数与密码到期时间，避免被判定为「密码过期」。
     */
    private void applyReset(SysUser user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.INITIAL);
        user.setPwdFailCount(0);
        user.setPwdUpdateTime(null);
        user.setOtpExpireTime(LocalDateTime.now().plusHours(resetValidHours));
        userRepository.save(user);
    }

    /**
     * 生成一次性随机密码：至少包含一个大写字母、一个小写字母和一个数字；
     * 随机打乱顺序，且不含易混淆字符。
     */
    private String generateOneTimePassword() {
        int length = Math.max(8, resetPasswordLength);
        List<Character> chars = new ArrayList<>(length);
        chars.add(randomChar(UPPER));
        chars.add(randomChar(LOWER));
        chars.add(randomChar(DIGITS));
        while (chars.size() < length) {
            chars.add(randomChar(ALL_CHARS));
        }
        Collections.shuffle(chars, SECURE_RANDOM);

        StringBuilder sb = new StringBuilder(length);
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    private char randomChar(String source) {
        return source.charAt(SECURE_RANDOM.nextInt(source.length()));
    }
}
