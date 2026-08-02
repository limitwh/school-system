package com.school.service;

import com.school.dto.LoginRequest;
import com.school.dto.LoginResponse;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 登录：校验账号密码，返回 JWT
     */
    LoginResponse login(LoginRequest request);
}
