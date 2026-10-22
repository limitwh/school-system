package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体，对应表 sys_user
 */
@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录名（管理员/校长自定义；师生账号默认 = 学号/职工号） */
    @Column(nullable = false, unique = true)
    private String username;

    /** 登录号：学生=学号，教职工=职工号 */
    @Column(name = "login_no", unique = true)
    private String loginNo;

    /** BCrypt 加密密码 */
    @Column(nullable = false)
    private String password;

    @Column(name = "real_name", nullable = false)
    private String realName;

    /** 联系电话：手机号或固定电话 */
    private String phone;

    private String email;

    /** 状态：0初始 1正常 2请假 3休学 4转出 5离职 6病假 7退休 8毕业 */
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "pwd_fail_count")
    private Integer pwdFailCount = 0;

    @Column(name = "pwd_update_time")
    private LocalDateTime pwdUpdateTime;

    /** 一次性密码到期时间：管理员重置密码后写入，超时则需重新重置；设置正式密码后清空 */
    @Column(name = "otp_expire_time")
    private LocalDateTime otpExpireTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 角色集合（通过 sys_user_role 关联） */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<SysRole> roles = new HashSet<>();

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Integer getPwdFailCount() {
        return pwdFailCount;
    }

    public void setPwdFailCount(Integer pwdFailCount) {
        this.pwdFailCount = pwdFailCount;
    }

    public LocalDateTime getPwdUpdateTime() {
        return pwdUpdateTime;
    }

    public void setPwdUpdateTime(LocalDateTime pwdUpdateTime) {
        this.pwdUpdateTime = pwdUpdateTime;
    }

    public LocalDateTime getOtpExpireTime() {
        return otpExpireTime;
    }

    public void setOtpExpireTime(LocalDateTime otpExpireTime) {
        this.otpExpireTime = otpExpireTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<SysRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<SysRole> roles) {
        this.roles = roles;
    }
}
