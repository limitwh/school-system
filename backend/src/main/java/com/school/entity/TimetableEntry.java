package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 课表条目：某班周几第几节上什么课
 */
@Entity
@Table(name = "timetable_entry",
        uniqueConstraints = @UniqueConstraint(name = "uk_entry_timetable_slot",
                columnNames = {"timetable_id", "weekday", "period_no"}))
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timetable_id", nullable = false)
    private Long timetableId;

    /** 星期：1~5（周一~周五） */
    @Column(nullable = false)
    private Integer weekday;

    @Column(name = "period_no", nullable = false)
    private Integer periodNo;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    /** 任课教师：第一版留空，后续启用 */
    @Column(name = "teacher_id")
    private Long teacherId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(Long timetableId) {
        this.timetableId = timetableId;
    }

    public Integer getWeekday() {
        return weekday;
    }

    public void setWeekday(Integer weekday) {
        this.weekday = weekday;
    }

    public Integer getPeriodNo() {
        return periodNo;
    }

    public void setPeriodNo(Integer periodNo) {
        this.periodNo = periodNo;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
