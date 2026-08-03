package com.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 初始状态用户设置密码请求
 */
public class SetPasswordRequest {

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "密码长度为 6-32 位")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
