package com.school.service;

import com.school.dto.AdminUserResponse;
import com.school.dto.ResetPasswordRequest;
import com.school.dto.ResetPasswordResult;

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

    /**
     * 按登录名重置任意用户密码：自动分配默认密码，状态置为「初始(0)」
     *
     * @param operatorId 当前操作的管理员 ID（用于阻止重置自己）
     * @param loginName  登录名：用户名 / 学号 / 职工号，三者均可
     */
    ResetPasswordResult resetPasswordByLoginName(Long operatorId, String loginName);
}
