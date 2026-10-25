package com.school.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 保存年级课程计划的单项
 */
public class GradePlanItem {

    @NotNull(message = "请选择科目")
    private Long subjectId;

    @NotNull(message = "请输入周节数")
    @PositiveOrZero(message = "周节数不能为负数")
    private Integer weeklyPeriods;

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
