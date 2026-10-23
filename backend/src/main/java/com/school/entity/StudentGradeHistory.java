package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 学生年级/班级变更历史，对应表 student_grade_history
 * <p>
 * 记录转学、留级、跳级、毕业等变更，便于追溯。
 */
@Entity
@Table(name = "student_grade_history")
public class StudentGradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "from_class_id")
    private Long fromClassId;

    @Column(name = "to_class_id")
    private Long toClassId;

    /** 变更类型：PROMOTE 升级 / TRANSFER_IN 转入 / REPEAT 留级 / SKIP 跳级 / ADJUST 手工调整 / GRADUATE 毕业 */
    @Column(name = "change_type", nullable = false, length = 20)
    private String changeType;

    @Column(name = "school_year_id")
    private Long schoolYearId;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(length = 200)
    private String remark;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getFromClassId() {
        return fromClassId;
    }

    public void setFromClassId(Long fromClassId) {
        this.fromClassId = fromClassId;
    }

    public Long getToClassId() {
        return toClassId;
    }

    public void setToClassId(Long toClassId) {
        this.toClassId = toClassId;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public Long getSchoolYearId() {
        return schoolYearId;
    }

    public void setSchoolYearId(Long schoolYearId) {
        this.schoolYearId = schoolYearId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
