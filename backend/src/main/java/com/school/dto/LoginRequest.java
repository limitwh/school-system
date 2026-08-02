package com.school.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求
 */
public class LoginRequest {

    /** 账号：用户名 或 学号/职工号 */
    @NotBlank(message = "请输入账号")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
