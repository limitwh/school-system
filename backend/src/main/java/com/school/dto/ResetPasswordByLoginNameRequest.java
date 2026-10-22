package com.school.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理员按登录名重置密码请求
 */
public class ResetPasswordByLoginNameRequest {

    /** 登录名：用户名 / 学号 / 职工号，三者均可 */
    @NotBlank(message = "请输入用户登录名")
    private String loginName;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
