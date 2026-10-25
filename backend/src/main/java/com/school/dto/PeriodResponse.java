package com.school.dto;

import java.time.LocalTime;

/**
 * 节次作息
 */
public class PeriodResponse {

    private Integer periodNo;
    private LocalTime startTime;
    private LocalTime endTime;

    public PeriodResponse() {
    }

    public PeriodResponse(Integer periodNo, LocalTime startTime, LocalTime endTime) {
        this.periodNo = periodNo;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getPeriodNo() {
        return periodNo;
    }

    public void setPeriodNo(Integer periodNo) {
        this.periodNo = periodNo;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
}
