package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 操作日志实体，对应表 sys_operation_log
 * <p>
 * 记录「谁 在什么时间 做了什么操作」，用于安全审计与问题追溯。
 */
@Entity
@Table(name = "sys_operation_log")
public class SysOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 操作人用户 ID（登录失败且账号不存在时为空） */
    @Column(name = "user_id")
    private Long userId;

    /** 操作人登录名（快照，登录失败时记录尝试登录的账号） */
    @Column(nullable = false, length = 50)
    private String username;

    /** 操作人姓名（快照） */
    @Column(name = "real_name", length = 50)
    private String realName;

    /** 所属模块：AUTH / USER / ... */
    @Column(nullable = false, length = 30)
    private String module;

    /** 操作类型：LOGIN / LOGOUT / CHANGE_PASSWORD / RESET_PASSWORD / ... */
    @Column(nullable = false, length = 50)
    private String action;

    /** 操作详情（成功说明或失败原因） */
    @Column(length = 500)
    private String detail;

    /** 是否成功 */
    @Column(nullable = false)
    private Boolean success;

    /** 来源 IP */
    @Column(length = 64)
    private String ip;

    /** 客户端 User-Agent */
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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
