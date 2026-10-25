package com.school.dto;

/**
 * 课表条目
 */
public class TimetableEntryResponse {

    private Integer weekday;
    private Integer periodNo;
    private Long subjectId;
    private String subjectName;

    public TimetableEntryResponse() {
    }

    public TimetableEntryResponse(Integer weekday, Integer periodNo, Long subjectId, String subjectName) {
        this.weekday = weekday;
        this.periodNo = periodNo;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
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

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
