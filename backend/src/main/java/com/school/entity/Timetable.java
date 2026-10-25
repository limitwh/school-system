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
 * 班级课表头：一个班在一个学年/学期一张课表
 */
@Entity
@Table(name = "timetable",
        uniqueConstraints = @UniqueConstraint(name = "uk_timetable_class_year_term",
                columnNames = {"class_id", "school_year_id", "term"}))
public class Timetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "school_year_id", nullable = false)
    private Long schoolYearId;

    /** 学期：1 上学期 / 2 下学期 */
    @Column(nullable = false)
    private Integer term;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getSchoolYearId() {
        return schoolYearId;
    }

    public void setSchoolYearId(Long schoolYearId) {
        this.schoolYearId = schoolYearId;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
