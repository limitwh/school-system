package com.school.dto;

import java.util.List;

/**
 * 当前登录用户信息（不包含敏感字段）
 */
public class CurrentUserResponse {

    private Long userId;
    private String username;
    private String realName;
    private String loginNo;
    private Integer status;
    private List<String> roles;

    public CurrentUserResponse() {
    }

    public CurrentUserResponse(Long userId, String username, String realName, String loginNo, Integer status, List<String> roles) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.loginNo = loginNo;
        this.status = status;
        this.roles = roles;
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

    public String getLoginNo() {
        return loginNo;
    }

    public void setLoginNo(String loginNo) {
        this.loginNo = loginNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
