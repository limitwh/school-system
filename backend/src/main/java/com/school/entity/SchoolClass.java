package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * 班级实体，对应表 class
 */
@Entity
@Table(name = "class",
        uniqueConstraints = @UniqueConstraint(name = "uk_class_grade_name",
                columnNames = {"grade_id", "name"}))
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 班级名称，如「一年级1班」 */
    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    /** 是否已毕业归档 */
    @Column(name = "is_graduated")
    private Boolean graduated = false;

    @Column(name = "graduate_time")
    private LocalDateTime graduateTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Boolean getGraduated() {
        return graduated;
    }

    public void setGraduated(Boolean graduated) {
        this.graduated = graduated;
    }

    public LocalDateTime getGraduateTime() {
        return graduateTime;
    }

    public void setGraduateTime(LocalDateTime graduateTime) {
        this.graduateTime = graduateTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
