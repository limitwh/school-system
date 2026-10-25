package com.school.dto;

import java.util.List;

/**
 * 班级课表
 */
public class TimetableResponse {

    private Long classId;
    private String className;
    private Long gradeId;
    private String gradeName;
    /** PRIMARY / JUNIOR */
    private String stage;
    private Long schoolYearId;
    private Integer term;
    private List<PeriodResponse> periods;
    private List<TimetableEntryResponse> entries;

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
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

    public List<PeriodResponse> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PeriodResponse> periods) {
        this.periods = periods;
    }

    public List<TimetableEntryResponse> getEntries() {
        return entries;
    }

    public void setEntries(List<TimetableEntryResponse> entries) {
        this.entries = entries;
    }
}
