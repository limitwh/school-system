package com.school.service;

import com.school.dto.ChangePasswordRequest;
import com.school.dto.LoginRequest;
import com.school.dto.LoginResponse;
import com.school.dto.SetPasswordRequest;
import com.school.security.LoginUser;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 登录：校验账号密码，返回 JWT
     */
    LoginResponse login(LoginRequest request);

    /**
     * 初始状态用户设置密码：成功后状态置为「正常」
     */
    void setPassword(LoginUser loginUser, SetPasswordRequest request);

    /**
     * 修改密码：校验原密码，成功后更新密码并记录到期时间、状态置为「正常」
     */
    void changePassword(LoginUser loginUser, ChangePasswordRequest request);

    /**
     * 退出登录：JWT 无状态，服务端不维护会话，仅记录操作日志
     *
     * @param reason 退出原因：MANUAL 手动 / IDLE 无操作超时
     */
    void logout(LoginUser loginUser, String reason);
}
