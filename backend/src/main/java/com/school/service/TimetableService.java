package com.school.service;

import com.school.dto.GradePlanItem;
import com.school.dto.GradePlanRow;
import com.school.dto.PeriodResponse;
import com.school.dto.SaveTimetableRequest;
import com.school.dto.SaveTimetableResult;
import com.school.dto.SubjectResponse;
import com.school.dto.TimetableResponse;

import java.util.List;

/**
 * 课程表服务
 */
public interface TimetableService {

    /** 科目列表 */
    List<SubjectResponse> listSubjects();

    /** 年级课程计划（含未配置科目，节数为 0） */
    List<GradePlanRow> getGradePlan(Long gradeId);

    /** 保存年级课程计划（覆盖式） */
    void saveGradePlan(Long gradeId, List<GradePlanItem> items);

    /** 节次作息 */
    List<PeriodResponse> listPeriods(String stage);

    /** 班级课表 */
    TimetableResponse getTimetable(Long classId, Long schoolYearId, Integer term);

    /** 保存班级课表，返回与年级计划的差异警告 */
    SaveTimetableResult saveTimetable(SaveTimetableRequest request);
}
