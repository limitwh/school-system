package com.school.service.impl;

import com.school.common.BusinessException;
import com.school.dto.GradePlanItem;
import com.school.dto.GradePlanRow;
import com.school.dto.PeriodResponse;
import com.school.dto.SaveTimetableRequest;
import com.school.dto.SaveTimetableResult;
import com.school.dto.SubjectResponse;
import com.school.dto.TimetableEntryResponse;
import com.school.dto.TimetableResponse;
import com.school.entity.Grade;
import com.school.entity.GradeSubject;
import com.school.entity.Period;
import com.school.entity.SchoolClass;
import com.school.entity.Subject;
import com.school.entity.Timetable;
import com.school.entity.TimetableEntry;
import com.school.repository.GradeRepository;
import com.school.repository.GradeSubjectRepository;
import com.school.repository.PeriodRepository;
import com.school.repository.SchoolClassRepository;
import com.school.repository.SchoolYearRepository;
import com.school.repository.SubjectRepository;
import com.school.repository.TimetableEntryRepository;
import com.school.repository.TimetableRepository;
import com.school.service.OperationLogService;
import com.school.service.TimetableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课程表服务实现
 */
@Service
public class TimetableServiceImpl implements TimetableService {

    private final SubjectRepository subjectRepository;
    private final GradeSubjectRepository gradeSubjectRepository;
    private final PeriodRepository periodRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository entryRepository;
    private final SchoolClassRepository classRepository;
    private final GradeRepository gradeRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final OperationLogService operationLogService;

    public TimetableServiceImpl(SubjectRepository subjectRepository,
                                GradeSubjectRepository gradeSubjectRepository,
                                PeriodRepository periodRepository,
                                TimetableRepository timetableRepository,
                                TimetableEntryRepository entryRepository,
                                SchoolClassRepository classRepository,
                                GradeRepository gradeRepository,
                                SchoolYearRepository schoolYearRepository,
                                OperationLogService operationLogService) {
        this.subjectRepository = subjectRepository;
        this.gradeSubjectRepository = gradeSubjectRepository;
        this.periodRepository = periodRepository;
        this.timetableRepository = timetableRepository;
        this.entryRepository = entryRepository;
        this.classRepository = classRepository;
        this.gradeRepository = gradeRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> listSubjects() {
        return subjectRepository.findAllByOrderBySortOrderAsc().stream()
                .map(s -> new SubjectResponse(s.getId(), s.getName(), s.getCode(), s.getCategory()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradePlanRow> getGradePlan(Long gradeId) {
        requireGrade(gradeId);

        Map<Long, Integer> plan = new HashMap<>();
        for (GradeSubject item : gradeSubjectRepository.findByGradeId(gradeId)) {
            plan.put(item.getSubjectId(), item.getWeeklyPeriods());
        }

        return subjectRepository.findAllByOrderBySortOrderAsc().stream()
                .map(s -> new GradePlanRow(s.getId(), s.getName(), s.getCode(), s.getCategory(),
                        plan.getOrDefault(s.getId(), 0)))
                .toList();
    }

    @Override
    @Transactional
    public void saveGradePlan(Long gradeId, List<GradePlanItem> items) {
        Grade grade = requireGrade(gradeId);

        Set<Long> seen = new HashSet<>();
        if (items == null) {
            throw new BusinessException(400, "课程计划不能为空");
        }
        for (GradePlanItem item : items) {
            if (item.getSubjectId() == null) {
                throw new BusinessException(400, "科目不能为空");
            }
            if (item.getWeeklyPeriods() == null) {
                throw new BusinessException(400, "周节数不能为空");
            }
            if (!seen.add(item.getSubjectId())) {
                throw new BusinessException(400, "同一科目不能重复配置");
            }
            if (!subjectRepository.existsById(item.getSubjectId())) {
                throw new BusinessException(400, "科目不存在：" + item.getSubjectId());
            }
        }

        gradeSubjectRepository.deleteByGradeId(gradeId);
        gradeSubjectRepository.flush();

        List<GradeSubject> rows = new ArrayList<>();
        for (GradePlanItem item : items) {
            if (item.getWeeklyPeriods() <= 0) {
                continue;
            }
            GradeSubject row = new GradeSubject();
            row.setGradeId(gradeId);
            row.setSubjectId(item.getSubjectId());
            row.setWeeklyPeriods(item.getWeeklyPeriods());
            rows.add(row);
        }
        gradeSubjectRepository.saveAll(rows);

        operationLogService.logCurrent("TIMETABLE", "SAVE_GRADE_PLAN",
                "保存年级课程计划：" + grade.getName() + "（" + rows.size() + " 门课）", true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodResponse> listPeriods(String stage) {
        if (!"PRIMARY".equals(stage) && !"JUNIOR".equals(stage)) {
            throw new BusinessException(400, "学段不合法：" + stage);
        }
        return periodRepository.findByStageOrderByPeriodNoAsc(stage).stream()
                .map(p -> new PeriodResponse(p.getPeriodNo(), p.getStartTime(), p.getEndTime()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableResponse getTimetable(Long classId, Long schoolYearId, Integer term) {
        SchoolClass clazz = requireClass(classId);
        Grade grade = requireGrade(clazz.getGradeId());

        TimetableResponse response = new TimetableResponse();
        response.setClassId(clazz.getId());
        response.setClassName(clazz.getName());
        response.setGradeId(grade.getId());
        response.setGradeName(grade.getName());
        response.setStage(grade.getStage());
        response.setSchoolYearId(schoolYearId);
        response.setTerm(term);

        List<PeriodResponse> periods = periodRepository.findByStageOrderByPeriodNoAsc(grade.getStage()).stream()
                .map(p -> new PeriodResponse(p.getPeriodNo(), p.getStartTime(), p.getEndTime()))
                .toList();
        response.setPeriods(periods);

        Map<Long, String> subjectNames = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));

        List<TimetableEntryResponse> entries = new ArrayList<>();
        timetableRepository.findByClassIdAndSchoolYearIdAndTerm(classId, schoolYearId, term)
                .ifPresent(timetable -> {
                    for (TimetableEntry item : entryRepository.findByTimetableId(timetable.getId())) {
                        entries.add(new TimetableEntryResponse(item.getWeekday(), item.getPeriodNo(),
                                item.getSubjectId(), subjectNames.get(item.getSubjectId())));
                    }
                });
        entries.sort(Comparator.comparing(TimetableEntryResponse::getWeekday)
                .thenComparing(TimetableEntryResponse::getPeriodNo));
        response.setEntries(entries);

        return response;
    }

    @Override
    @Transactional
    public SaveTimetableResult saveTimetable(SaveTimetableRequest request) {
        SchoolClass clazz = requireClass(request.getClassId());
        Grade grade = requireGrade(clazz.getGradeId());
        if (!schoolYearRepository.existsById(request.getSchoolYearId())) {
            throw new BusinessException(400, "学年不存在");
        }

        List<Period> periods = periodRepository.findByStageOrderByPeriodNoAsc(grade.getStage());
        int maxPeriod = periods.stream().mapToInt(Period::getPeriodNo).max().orElse(0);
        if (maxPeriod == 0) {
            throw new BusinessException(500, "缺少 " + grade.getStage() + " 学段的作息配置");
        }

        Map<Long, Subject> subjects = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));

        Set<String> slots = new HashSet<>();
        if (request.getEntries() == null) {
            throw new BusinessException(400, "课表内容不能为空");
        }
        for (SaveTimetableRequest.Entry item : request.getEntries()) {
            if (item.getWeekday() == null || item.getPeriodNo() == null || item.getSubjectId() == null) {
                throw new BusinessException(400, "课表条目不完整");
            }
            if (item.getWeekday() < 1 || item.getWeekday() > 5) {
                throw new BusinessException(400, "星期不合法：" + item.getWeekday());
            }
            if (item.getPeriodNo() < 1 || item.getPeriodNo() > maxPeriod) {
                throw new BusinessException(400,
                        "节次超出范围：第 " + item.getPeriodNo() + " 节（本学段共 " + maxPeriod + " 节）");
            }
            if (!subjects.containsKey(item.getSubjectId())) {
                throw new BusinessException(400, "科目不存在：" + item.getSubjectId());
            }
            if (!slots.add(item.getWeekday() + "-" + item.getPeriodNo())) {
                throw new BusinessException(400, "同一时间只能安排一门课");
            }
        }

        Timetable timetable = timetableRepository
                .findByClassIdAndSchoolYearIdAndTerm(request.getClassId(),
                        request.getSchoolYearId(), request.getTerm())
                .orElseGet(() -> {
                    Timetable created = new Timetable();
                    created.setClassId(request.getClassId());
                    created.setSchoolYearId(request.getSchoolYearId());
                    created.setTerm(request.getTerm());
                    created.setCreatedAt(LocalDateTime.now());
                    return timetableRepository.save(created);
                });

        entryRepository.deleteByTimetableId(timetable.getId());
        entryRepository.flush();

        List<TimetableEntry> rows = new ArrayList<>();
        for (SaveTimetableRequest.Entry item : request.getEntries()) {
            TimetableEntry entry = new TimetableEntry();
            entry.setTimetableId(timetable.getId());
            entry.setWeekday(item.getWeekday());
            entry.setPeriodNo(item.getPeriodNo());
            entry.setSubjectId(item.getSubjectId());
            rows.add(entry);
        }
        entryRepository.saveAll(rows);

        List<String> warnings = buildWarnings(grade.getId(), rows, subjects);

        operationLogService.logCurrent("TIMETABLE", "SAVE_TIMETABLE",
                "保存课表：" + clazz.getName() + "，学年 " + request.getSchoolYearId()
                        + "，学期 " + request.getTerm() + "，共 " + rows.size() + " 节", true);

        return new SaveTimetableResult(warnings);
    }

    /**
     * 对比年级课程计划与实际课表，返回差异警告
     */
    private List<String> buildWarnings(Long gradeId, List<TimetableEntry> rows, Map<Long, Subject> subjects) {
        List<GradeSubject> plan = gradeSubjectRepository.findByGradeId(gradeId);
        Map<Long, Long> actual = rows.stream()
                .collect(Collectors.groupingBy(TimetableEntry::getSubjectId, Collectors.counting()));

        List<String> warnings = new ArrayList<>();
        for (GradeSubject item : plan) {
            long count = actual.getOrDefault(item.getSubjectId(), 0L);
            if (count != item.getWeeklyPeriods()) {
                warnings.add(subjectName(subjects, item.getSubjectId())
                        + "：计划 " + item.getWeeklyPeriods() + " 节，实际 " + count + " 节");
            }
        }

        Set<Long> planned = plan.stream().map(GradeSubject::getSubjectId).collect(Collectors.toSet());
        for (Map.Entry<Long, Long> item : actual.entrySet()) {
            if (!planned.contains(item.getKey())) {
                warnings.add(subjectName(subjects, item.getKey())
                        + "：未在年级计划中（实际 " + item.getValue() + " 节）");
            }
        }
        return warnings;
    }

    private String subjectName(Map<Long, Subject> subjects, Long subjectId) {
        Subject subject = subjects.get(subjectId);
        return subject == null ? ("科目" + subjectId) : subject.getName();
    }

    private SchoolClass requireClass(Long classId) {
        return classRepository.findById(classId)
                .orElseThrow(() -> new BusinessException(400, "班级不存在"));
    }

    private Grade requireGrade(Long gradeId) {
        return gradeRepository.findById(gradeId)
                .orElseThrow(() -> new BusinessException(400, "年级不存在"));
    }
}
