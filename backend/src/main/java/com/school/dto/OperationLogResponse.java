package com.school.dto;

import com.school.entity.SysOperationLog;

import java.time.LocalDateTime;

/**
 * 操作日志展示对象
 */
public class OperationLogResponse {

    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String module;
    private String action;
    private String detail;
    private Boolean success;
    private String ip;
    private String userAgent;
    private LocalDateTime createdAt;

    public static OperationLogResponse from(SysOperationLog log) {
        OperationLogResponse resp = new OperationLogResponse();
        resp.setId(log.getId());
        resp.setUserId(log.getUserId());
        resp.setUsername(log.getUsername());
        resp.setRealName(log.getRealName());
        resp.setModule(log.getModule());
        resp.setAction(log.getAction());
        resp.setDetail(log.getDetail());
        resp.setSuccess(log.getSuccess());
        resp.setIp(log.getIp());
        resp.setUserAgent(log.getUserAgent());
        resp.setCreatedAt(log.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
