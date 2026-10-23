package com.school.dto;

/**
 * 用户简要信息（消息联系人、会话对方）
 */
public class UserBrief {

    private Long userId;
    private String username;
    private String realName;

    public UserBrief() {
    }

    public UserBrief(Long userId, String username, String realName) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
