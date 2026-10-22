package com.school.dto;

/**
 * 退出登录请求
 */
public class LogoutRequest {

    /** 退出原因：MANUAL 手动退出 / IDLE 长时间未操作自动退出 */
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
