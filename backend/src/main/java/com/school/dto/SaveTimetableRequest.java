package com.school.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 保存班级课表请求
 */
public class SaveTimetableRequest {

    @NotNull(message = "请选择班级")
    private Long classId;

    @NotNull(message = "请选择学年")
    private Long schoolYearId;

    @NotNull(message = "请选择学期")
    @Min(value = 1, message = "学期只能是 1 或 2")
    @Max(value = 2, message = "学期只能是 1 或 2")
    private Integer term;

    @NotNull(message = "课表内容不能为空")
    @Valid
    private List<Entry> entries;

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

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    /** 单个格子 */
    public static class Entry {

        @NotNull(message = "缺少星期")
        private Integer weekday;

        @NotNull(message = "缺少节次")
        private Integer periodNo;

        @NotNull(message = "缺少科目")
        private Long subjectId;

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
    }
}
