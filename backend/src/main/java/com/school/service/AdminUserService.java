package com.school.service;

import com.school.dto.AdminUserResponse;
import com.school.dto.ResetPasswordRequest;

import java.util.List;

/**
 * 管理员用户管理服务
 */
public interface AdminUserService {

    /**
     * 查询所有锁定状态（status=9）的用户
     */
    List<AdminUserResponse> listLockedUsers();

    /**
     * 重置锁定用户：状态置为「初始(0)」并分配初始密码
     */
    void resetPassword(Long userId, ResetPasswordRequest request);
}
