package com.school.dto;

import java.util.List;

/**
 * 保存课表结果：课时与计划的差异以警告形式返回
 */
public class SaveTimetableResult {

    private List<String> warnings;

    public SaveTimetableResult() {
    }

    public SaveTimetableResult(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
