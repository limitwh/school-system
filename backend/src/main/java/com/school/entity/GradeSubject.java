package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 年级课程计划：每个年级每门课每周几节
 */
@Entity
@Table(name = "grade_subject",
        uniqueConstraints = @UniqueConstraint(name = "uk_grade_subject",
                columnNames = {"grade_id", "subject_id"}))
public class GradeSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grade_id", nullable = false)
    private Long gradeId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "weekly_periods", nullable = false)
    private Integer weeklyPeriods;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getWeeklyPeriods() {
        return weeklyPeriods;
    }

    public void setWeeklyPeriods(Integer weeklyPeriods) {
        this.weeklyPeriods = weeklyPeriods;
    }
}
