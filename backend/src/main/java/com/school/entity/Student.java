package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 学生实体，对应表 student
 * <p>
 * 学生只保存 class_id，年级通过 class.grade_id 关联，避免两处维护。
 */
@Entity
@Table(name = "student")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /** 学号，全局唯一 */
    @Column(name = "student_no", unique = true, length = 20)
    private String studentNo;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    /** 入学年份（届别管理、学号生成）；不用于推算年级 */
    @Column(name = "enroll_year", nullable = false)
    private Integer enrollYear;

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Integer getEnrollYear() {
        return enrollYear;
    }

    public void setEnrollYear(Integer enrollYear) {
        this.enrollYear = enrollYear;
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
