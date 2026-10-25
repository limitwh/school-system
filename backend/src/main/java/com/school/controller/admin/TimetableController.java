package com.school.controller.admin;

import com.school.common.Result;
import com.school.dto.GradePlanItem;
import com.school.dto.GradePlanRow;
import com.school.dto.PeriodResponse;
import com.school.dto.SaveTimetableRequest;
import com.school.dto.SaveTimetableResult;
import com.school.dto.SubjectResponse;
import com.school.dto.TimetableResponse;
import com.school.service.TimetableService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员 - 课程表（科目 / 年级计划 / 节次 / 班级课表）
 */
@RestController
@RequestMapping("/api/admin/timetable")
@PreAuthorize("hasRole('ADMIN')")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    /** 科目列表 */
    @GetMapping("/subjects")
    public Result<List<SubjectResponse>> listSubjects() {
        return Result.success(timetableService.listSubjects());
    }

    /** 年级课程计划 */
    @GetMapping("/plans/{gradeId}")
    public Result<List<GradePlanRow>> getGradePlan(@PathVariable Long gradeId) {
        return Result.success(timetableService.getGradePlan(gradeId));
    }

    /** 保存年级课程计划 */
    @PutMapping("/plans/{gradeId}")
    public Result<Void> saveGradePlan(@PathVariable Long gradeId,
                                      @RequestBody List<GradePlanItem> items) {
        timetableService.saveGradePlan(gradeId, items);
        return Result.success();
    }

    /** 节次作息 */
    @GetMapping("/periods")
    public Result<List<PeriodResponse>> listPeriods(@RequestParam String stage) {
        return Result.success(timetableService.listPeriods(stage));
    }

    /** 班级课表 */
    @GetMapping
    public Result<TimetableResponse> getTimetable(@RequestParam Long classId,
                                                  @RequestParam Long schoolYearId,
                                                  @RequestParam Integer term) {
        return Result.success(timetableService.getTimetable(classId, schoolYearId, term));
    }

    /** 保存班级课表 */
    @PutMapping
    public Result<SaveTimetableResult> saveTimetable(@RequestBody @Valid SaveTimetableRequest request) {
        return Result.success(timetableService.saveTimetable(request));
    }
}
