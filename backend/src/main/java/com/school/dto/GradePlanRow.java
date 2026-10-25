package com.school.dto;

/**
 * 年级课程计划行（含未配置的科目，节数为 0）
 */
public class GradePlanRow {

    private Long subjectId;
    private String name;
    private String code;
    private String category;
    private int weeklyPeriods;

    public GradePlanRow() {
    }

    public GradePlanRow(Long subjectId, String name, String code, String category, int weeklyPeriods) {
        this.subjectId = subjectId;
        this.name = name;
        this.code = code;
        this.category = category;
        this.weeklyPeriods = weeklyPeriods;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getWeeklyPeriods() {
        return weeklyPeriods;
    }

    public void setWeeklyPeriods(int weeklyPeriods) {
        this.weeklyPeriods = weeklyPeriods;
    }
}
