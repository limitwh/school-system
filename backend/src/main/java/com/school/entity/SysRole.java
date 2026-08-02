package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 角色实体，对应表 sys_role
 */
@Entity
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 角色编码：ADMIN / PRINCIPAL / GRADE_ADMIN / TEACHER / STUDENT */
    @Column(nullable = false, unique = true)
    private String code;

    /** 角色名称：系统管理员 / 校长 / 年级管理员 / 教师 / 学生 */
    @Column(nullable = false)
    private String name;

    /** 数据范围：ALL / ALL_STAFF_STUDENT / GRADE / CLASS / SELF */
    @Column(name = "data_scope")
    private String dataScope;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
