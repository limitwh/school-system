package com.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理员重置密码请求：为锁定用户分配初始密码
 */
public class ResetPasswordRequest {

    @NotBlank(message = "请输入初始密码")
    @Size(min = 6, max = 32, message = "密码长度为 6-32 位")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
