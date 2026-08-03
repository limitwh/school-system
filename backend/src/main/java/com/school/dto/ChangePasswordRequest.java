package com.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求（用户修改自己的密码）
 */
public class ChangePasswordRequest {

    @NotBlank(message = "请输入原密码")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "密码长度为 6-32 位")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
