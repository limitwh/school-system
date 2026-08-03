package com.school.service.impl;

import com.school.common.BusinessException;
import com.school.common.UserStatus;
import com.school.dto.AdminUserResponse;
import com.school.dto.ResetPasswordRequest;
import com.school.entity.SysUser;
import com.school.repository.SysUserRepository;
import com.school.service.AdminUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员用户管理服务实现
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(SysUserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        // 重置：分配初始密码，状态置为「初始(0)」，清除失败计数
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setStatus(UserStatus.INITIAL);
        user.setPwdFailCount(0);
        userRepository.save(user);
    }
}
