package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 年级实体，对应表 grade
 * <p>
 * 九年一贯制：小学 6 个年级（PRIMARY，sort_order 1~6）+ 初中 3 个年级（JUNIOR，7~9）。
 * sort_order 连续，保证升年级时可自然跨越学段。
 */
@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /** 学段：PRIMARY 小学 / JUNIOR 初中 */
    @Column(nullable = false, length = 20)
    private String stage;

    /** 排序：一年级=1 … 六年级=6、初一=7 … 初三=9 */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /** 1 启用 / 0 停用 */
    @Column(nullable = false)
    private Integer enabled = 1;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
