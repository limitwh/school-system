package com.school.dto;

import java.time.LocalDateTime;

/**
 * 管理员视角的用户信息（用于锁定用户管理列表）
 */
public class AdminUserResponse {

    private Long id;
    private String username;
    private String loginNo;
    private String realName;
    private Integer status;
    private LocalDateTime lastLoginTime;

    public AdminUserResponse() {
    }

    public AdminUserResponse(Long id, String username, String loginNo, String realName,
                             Integer status, LocalDateTime lastLoginTime) {
        this.id = id;
        this.username = username;
        this.loginNo = loginNo;
        this.realName = realName;
        this.status = status;
        this.lastLoginTime = lastLoginTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginNo() {
        return loginNo;
    }

    public void setLoginNo(String loginNo) {
        this.loginNo = loginNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
