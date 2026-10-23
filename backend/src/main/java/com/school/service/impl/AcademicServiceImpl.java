package com.school.service.impl;

import com.school.common.BusinessException;
import com.school.common.UserStatus;
import com.school.dto.ClassRequest;
import com.school.dto.ClassResponse;
import com.school.dto.GradeResponse;
import com.school.dto.PageResult;
import com.school.dto.PromotionPreview;
import com.school.dto.PromotionResult;
import com.school.dto.SchoolYearRequest;
import com.school.dto.SchoolYearResponse;
import com.school.dto.StudentRequest;
import com.school.dto.StudentResponse;
import com.school.entity.Grade;
import com.school.entity.SchoolClass;
import com.school.entity.SchoolYear;
import com.school.entity.Student;
import com.school.entity.StudentGradeHistory;
import com.school.entity.SysRole;
import com.school.entity.SysUser;
import com.school.repository.GradeRepository;
import com.school.repository.SchoolClassRepository;
import com.school.repository.SchoolYearRepository;
import com.school.repository.StudentGradeHistoryRepository;
import com.school.repository.StudentRepository;
import com.school.repository.SysRoleRepository;
import com.school.repository.SysUserRepository;
import com.school.service.AcademicService;
import com.school.service.OperationLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学籍基础数据服务实现
 */
@Service
public class AcademicServiceImpl implements AcademicService {

    /** 新建学生账号的初始密码，首次登录会被强制修改 */
    private static final String DEFAULT_INITIAL_PASSWORD = "123456";

    private static final String CHANGE_TYPE_ADJUST = "ADJUST";
    private static final String CHANGE_TYPE_PROMOTE = "PROMOTE";
    private static final String CHANGE_TYPE_GRADUATE = "GRADUATE";

    private final GradeRepository gradeRepository;
    private final SchoolClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final StudentGradeHistoryRepository historyRepository;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationLogService operationLogService;

    public AcademicServiceImpl(GradeRepository gradeRepository,
                               SchoolClassRepository classRepository,
                               StudentRepository studentRepository,
                               SchoolYearRepository schoolYearRepository,
                               StudentGradeHistoryRepository historyRepository,
                               SysUserRepository userRepository,
                               SysRoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               OperationLogService operationLogService) {
        this.gradeRepository = gradeRepository;
        this.classRepository = classRepository;
        this.studentRepository = studentRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.operationLogService = operationLogService;
    }

    /* ================= 年级 ================= */

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> listGrades() {
        return gradeRepository.findAllByOrderBySortOrderAsc().stream()
                .map(g -> new GradeResponse(g.getId(), g.getName(), g.getStage(), g.getSortOrder(), g.getEnabled()))
                .toList();
    }

    /* ================= 班级 ================= */

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> listClasses() {
        Map<Long, String> gradeNames = gradeRepository.findAll().stream()
                .collect(Collectors.toMap(Grade::getId, Grade::getName));
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : studentRepository.countGroupByClass()) {
            counts.put((Long) row[0], ((Number) row[1]).longValue());
        }

        return classRepository.findAllByOrderByIdAsc().stream().map(c -> {
            ClassResponse response = new ClassResponse();
            response.setId(c.getId());
            response.setName(c.getName());
            response.setGradeId(c.getGradeId());
            response.setGradeName(gradeNames.get(c.getGradeId()));
            response.setGraduated(c.getGraduated());
            response.setStudentCount(counts.getOrDefault(c.getId(), 0L));
            return response;
        }).toList();
    }

    @Override
    @Transactional
    public ClassResponse createClass(ClassRequest request) {
        Grade grade = requireGrade(request.getGradeId());
        String name = request.getName().trim();

        if (classRepository.existsByGradeIdAndName(grade.getId(), name)) {
            throw new BusinessException(400, "该年级下已存在同名班级");
        }

        SchoolClass clazz = new SchoolClass();
        clazz.setName(name);
        clazz.setGradeId(grade.getId());
        clazz.setGraduated(false);
        clazz.setCreatedAt(LocalDateTime.now());
        classRepository.save(clazz);

        operationLogService.logCurrent("ACADEMIC", "CREATE_CLASS",
                "新增班级：" + grade.getName() + " " + name, true);

        return toClassResponse(clazz, grade.getName(), 0);
    }

    @Override
    @Transactional
    public ClassResponse updateClass(Long id, ClassRequest request) {
        SchoolClass clazz = classRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "班级不存在"));
        Grade grade = requireGrade(request.getGradeId());
        String name = request.getName().trim();

        if (classRepository.existsByGradeIdAndNameAndIdNot(grade.getId(), name, id)) {
            throw new BusinessException(400, "该年级下已存在同名班级");
        }

        clazz.setName(name);
        clazz.setGradeId(grade.getId());
        classRepository.save(clazz);

        operationLogService.logCurrent("ACADEMIC", "UPDATE_CLASS",
                "修改班级：" + grade.getName() + " " + name, true);

        return toClassResponse(clazz, grade.getName(), studentRepository.countByClassId(id));
    }

    /* ================= 学生 ================= */

    @Override
    @Transactional(readOnly = true)
    public PageResult<StudentResponse> listStudents(int page, int size, String keyword, Long classId) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

        Page<Student> pageData = studentRepository.search(kw, classId, PageRequest.of(pageNo - 1, pageSize));
        return new PageResult<>(pageData.getTotalElements(), pageNo, pageSize,
                toStudentResponses(pageData.getContent()));
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request, Long operatorId) {
        SchoolClass clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new BusinessException(400, "班级不存在"));
        if (Boolean.TRUE.equals(clazz.getGraduated())) {
            throw new BusinessException(400, "该班级已毕业，不能添加学生");
        }

        String studentNo = StringUtils.hasText(request.getStudentNo())
                ? request.getStudentNo().trim()
                : generateStudentNo(request.getEnrollYear());

        if (studentRepository.existsByStudentNo(studentNo) || userRepository.existsByUsername(studentNo)) {
            throw new BusinessException(400, "学号已存在：" + studentNo);
        }

        SysRole studentRole = roleRepository.findByCode("STUDENT")
                .orElseThrow(() -> new BusinessException(500, "系统缺少 STUDENT 角色"));

        SysUser user = new SysUser();
        user.setUsername(studentNo);
        user.setLoginNo(studentNo);
        user.setPassword(passwordEncoder.encode(DEFAULT_INITIAL_PASSWORD));
        user.setRealName(request.getRealName().trim());
        user.setStatus(UserStatus.INITIAL);
        user.setPwdFailCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.getRoles().add(studentRole);
        userRepository.save(user);

        Student student = new Student();
        student.setUserId(user.getId());
        student.setStudentNo(studentNo);
        student.setClassId(clazz.getId());
        student.setEnrollYear(request.getEnrollYear());
        student.setCreatedAt(LocalDateTime.now());
        studentRepository.save(student);

        operationLogService.logCurrent("ACADEMIC", "CREATE_STUDENT",
                "新增学生：" + user.getRealName() + "（" + studentNo + "）", true);

        return toStudentResponses(List.of(student)).get(0);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request, Long operatorId) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "学生不存在"));
        SchoolClass clazz = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new BusinessException(400, "班级不存在"));
        if (Boolean.TRUE.equals(clazz.getGraduated())) {
            throw new BusinessException(400, "该班级已毕业，不能调入学生");
        }

        // 班级变化：记录变更历史
        if (!clazz.getId().equals(student.getClassId())) {
            StudentGradeHistory history = new StudentGradeHistory();
            history.setStudentId(student.getId());
            history.setFromClassId(student.getClassId());
            history.setToClassId(clazz.getId());
            history.setChangeType(CHANGE_TYPE_ADJUST);
            schoolYearRepository.findByCurrentTrue().ifPresent(year -> history.setSchoolYearId(year.getId()));
            history.setOperatorId(operatorId);
            history.setRemark("手工调整班级");
            history.setCreatedAt(LocalDateTime.now());
            historyRepository.save(history);

            student.setClassId(clazz.getId());
        }

        student.setEnrollYear(request.getEnrollYear());
        studentRepository.save(student);

        SysUser user = userRepository.findById(student.getUserId())
                .orElseThrow(() -> new BusinessException(400, "学生账号不存在"));
        user.setRealName(request.getRealName().trim());
        userRepository.save(user);

        operationLogService.logCurrent("ACADEMIC", "UPDATE_STUDENT",
                "修改学生：" + user.getRealName() + "（" + student.getStudentNo() + "）", true);

        return toStudentResponses(List.of(student)).get(0);
    }

    /* ================= 学年 ================= */

    @Override
    @Transactional(readOnly = true)
    public List<SchoolYearResponse> listSchoolYears() {
        return schoolYearRepository.findAllByOrderByStartDateDesc().stream()
                .map(this::toSchoolYearResponse)
                .toList();
    }

    @Override
    @Transactional
    public SchoolYearResponse createSchoolYear(SchoolYearRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        String name = request.getName().trim();
        if (schoolYearRepository.findAll().stream().anyMatch(y -> y.getName().equals(name))) {
            throw new BusinessException(400, "学年已存在：" + name);
        }

        SchoolYear year = new SchoolYear();
        year.setName(name);
        year.setStartDate(request.getStartDate());
        year.setEndDate(request.getEndDate());
        year.setCurrent(false);
        schoolYearRepository.save(year);

        if (Boolean.TRUE.equals(request.getCurrent())) {
            markCurrent(year);
        }

        operationLogService.logCurrent("ACADEMIC", "CREATE_SCHOOL_YEAR", "新增学年：" + name, true);
        return toSchoolYearResponse(year);
    }

    @Override
    @Transactional
    public SchoolYearResponse setCurrentSchoolYear(Long id) {
        SchoolYear year = schoolYearRepository.findById(id)
                .orElseThrow(() -> new BusinessException(400, "学年不存在"));
        markCurrent(year);
        operationLogService.logCurrent("ACADEMIC", "SET_CURRENT_SCHOOL_YEAR",
                "设置当前学年：" + year.getName(), true);
        return toSchoolYearResponse(year);
    }

    /* ================= 升级 / 毕业 ================= */

    @Override
    @Transactional(readOnly = true)
    public PromotionPreview previewPromotion() {
        SchoolYear year = schoolYearRepository.findByCurrentTrue().orElse(null);

        PromotionPreview preview = new PromotionPreview();
        preview.setSchoolYear(year == null ? null : year.getName());
        if (year != null) {
            preview.setPromotionExecuted(historyRepository
                    .existsBySchoolYearIdAndChangeType(year.getId(), CHANGE_TYPE_PROMOTE));
            preview.setGraduationExecuted(historyRepository
                    .existsBySchoolYearIdAndChangeType(year.getId(), CHANGE_TYPE_GRADUATE));
        }

        List<Grade> grades = gradeRepository.findAllByOrderBySortOrderAsc();
        Map<Long, Grade> gradeById = grades.stream().collect(Collectors.toMap(Grade::getId, g -> g));
        Map<Integer, Grade> gradeBySort = grades.stream().collect(Collectors.toMap(Grade::getSortOrder, g -> g));
        Grade lastGrade = grades.isEmpty() ? null : grades.get(grades.size() - 1);

        List<SchoolClass> classes = new ArrayList<>(classRepository.findAll());
        classes.sort((a, b) -> Integer.compare(sortOf(a.getGradeId(), gradeById), sortOf(b.getGradeId(), gradeById)));

        List<PromotionPreview.PromotionItem> promotions = new ArrayList<>();
        List<PromotionPreview.GraduationItem> graduations = new ArrayList<>();
        int promotionStudents = 0;
        int graduationStudents = 0;

        for (SchoolClass clazz : classes) {
            if (Boolean.TRUE.equals(clazz.getGraduated())) {
                continue;
            }
            Grade grade = gradeById.get(clazz.getGradeId());
            if (grade == null) {
                continue;
            }
            long count = studentRepository.countByClassId(clazz.getId());

            if (lastGrade != null && grade.getSortOrder().equals(lastGrade.getSortOrder())) {
                graduations.add(new PromotionPreview.GraduationItem(
                        clazz.getId(), clazz.getName(), grade.getName(), count));
                graduationStudents += count;
            } else {
                Grade next = gradeBySort.get(grade.getSortOrder() + 1);
                if (next != null) {
                    promotions.add(new PromotionPreview.PromotionItem(
                            clazz.getId(), clazz.getName(), grade.getName(), next.getName(), count));
                    promotionStudents += count;
                }
            }
        }

        preview.setPromotions(promotions);
        preview.setGraduations(graduations);
        preview.setPromotionStudentCount(promotionStudents);
        preview.setGraduationStudentCount(graduationStudents);
        return preview;
    }

    @Override
    @Transactional
    public PromotionResult executePromotion(Long operatorId) {
        SchoolYear year = schoolYearRepository.findByCurrentTrue()
                .orElseThrow(() -> new BusinessException(400, "请先在学籍管理中设置当前学年"));

        boolean promotionDone = historyRepository
                .existsBySchoolYearIdAndChangeType(year.getId(), CHANGE_TYPE_PROMOTE);
        boolean graduationDone = historyRepository
                .existsBySchoolYearIdAndChangeType(year.getId(), CHANGE_TYPE_GRADUATE);

        if (promotionDone && graduationDone) {
            throw new BusinessException(400, "本学年已完成升级和毕业操作，无需重复执行");
        }

        PromotionResult result = new PromotionResult();
        result.setSchoolYear(year.getName());
        result.setPromotionSkipped(promotionDone);
        result.setGraduationSkipped(graduationDone);

        List<Grade> grades = gradeRepository.findAllByOrderBySortOrderAsc();
        Map<Long, Grade> gradeById = grades.stream().collect(Collectors.toMap(Grade::getId, g -> g));
        Map<Integer, Grade> gradeBySort = grades.stream().collect(Collectors.toMap(Grade::getSortOrder, g -> g));
        Grade lastGrade = grades.isEmpty() ? null : grades.get(grades.size() - 1);

        // 1) 毕业：最后一学段班级归档、学生状态置为毕业、班级名追加届别便于复用原名称
        if (!graduationDone && lastGrade != null) {
            int graduateYear = year.getEndDate().getYear();
            int graduatedClasses = 0;
            int graduatedStudents = 0;

            for (SchoolClass clazz : classRepository.findAll()) {
                if (Boolean.TRUE.equals(clazz.getGraduated()) || !lastGrade.getId().equals(clazz.getGradeId())) {
                    continue;
                }

                List<Student> students = studentRepository.findByClassId(clazz.getId());
                for (Student student : students) {
                    student.setGraduateTime(LocalDateTime.now());
                    studentRepository.save(student);

                    userRepository.findById(student.getUserId()).ifPresent(user -> {
                        user.setStatus(UserStatus.GRADUATED);
                        userRepository.save(user);
                    });
                    historyRepository.save(buildHistory(student.getId(), clazz.getId(), null,
                            CHANGE_TYPE_GRADUATE, year.getId(), operatorId, "毕业离校"));
                }

                clazz.setGraduated(true);
                clazz.setGraduateTime(LocalDateTime.now());
                clazz.setName(graduatedClassName(clazz.getName(), graduateYear));
                classRepository.save(clazz);

                graduatedClasses++;
                graduatedStudents += students.size();
            }

            result.setGraduatedClasses(graduatedClasses);
            result.setGraduatedStudents(graduatedStudents);
        }

        // 2) 升级：按年级从高到低处理，目标班级先腾出名称，避免唯一约束冲突
        if (!promotionDone) {
            List<SchoolClass> classes = new ArrayList<>(classRepository.findAll());
            classes.sort((a, b) -> Integer.compare(
                    sortOf(b.getGradeId(), gradeById), sortOf(a.getGradeId(), gradeById)));

            int promotedClasses = 0;
            int promotedStudents = 0;

            for (SchoolClass clazz : classes) {
                if (Boolean.TRUE.equals(clazz.getGraduated())) {
                    continue;
                }
                Grade grade = gradeById.get(clazz.getGradeId());
                if (grade == null || (lastGrade != null && grade.getSortOrder() >= lastGrade.getSortOrder())) {
                    continue;
                }
                Grade next = gradeBySort.get(grade.getSortOrder() + 1);
                if (next == null) {
                    continue;
                }

                String newName = renameForGrade(clazz.getName(), grade.getName(), next.getName());
                if (classRepository.existsByGradeIdAndNameAndIdNot(next.getId(), newName, clazz.getId())) {
                    throw new BusinessException(400,
                            "升级后班级名称冲突：" + newName + "，请先处理该年级下的历史班级");
                }

                List<Student> students = studentRepository.findByClassId(clazz.getId());
                for (Student student : students) {
                    historyRepository.save(buildHistory(student.getId(), clazz.getId(), clazz.getId(),
                            CHANGE_TYPE_PROMOTE, year.getId(), operatorId, "升入" + next.getName()));
                }

                clazz.setGradeId(next.getId());
                clazz.setName(newName);
                classRepository.save(clazz);

                promotedClasses++;
                promotedStudents += students.size();
            }

            result.setPromotedClasses(promotedClasses);
            result.setPromotedStudents(promotedStudents);
        }

        operationLogService.logCurrent("ACADEMIC", "PROMOTE_GRADUATE",
                "升级/毕业（" + year.getName() + "）：升级 " + result.getPromotedStudents()
                        + " 人，毕业 " + result.getGraduatedStudents() + " 人", true);

        return result;
    }

    private int sortOf(Long gradeId, Map<Long, Grade> gradeById) {
        Grade grade = gradeById.get(gradeId);
        return grade == null ? 0 : grade.getSortOrder();
    }

    /** 升级时替换班级名中的年级前缀：一年级1班 → 二年级1班 */
    private String renameForGrade(String className, String oldGradeName, String newGradeName) {
        return className.startsWith(oldGradeName)
                ? newGradeName + className.substring(oldGradeName.length())
                : className;
    }

    /** 毕业班级名追加届别：初三1班 → 初三1班（2026届），便于新一届复用原名称 */
    private String graduatedClassName(String className, int year) {
        return className.contains("届") ? className : className + "（" + year + "届）";
    }

    private StudentGradeHistory buildHistory(Long studentId, Long fromClassId, Long toClassId,
                                             String changeType, Long schoolYearId,
                                             Long operatorId, String remark) {
        StudentGradeHistory history = new StudentGradeHistory();
        history.setStudentId(studentId);
        history.setFromClassId(fromClassId);
        history.setToClassId(toClassId);
        history.setChangeType(changeType);
        history.setSchoolYearId(schoolYearId);
        history.setOperatorId(operatorId);
        history.setRemark(remark);
        history.setCreatedAt(LocalDateTime.now());
        return history;
    }

    /* ================= 内部方法 ================= */

    private void markCurrent(SchoolYear year) {
        List<SchoolYear> years = schoolYearRepository.findAll();
        for (SchoolYear item : years) {
            if (Boolean.TRUE.equals(item.getCurrent()) && !item.getId().equals(year.getId())) {
                item.setCurrent(false);
            }
        }
        year.setCurrent(true);
        schoolYearRepository.saveAll(years);
    }

    private Grade requireGrade(Long gradeId) {
        return gradeRepository.findById(gradeId)
                .orElseThrow(() -> new BusinessException(400, "年级不存在"));
    }

    private ClassResponse toClassResponse(SchoolClass clazz, String gradeName, long studentCount) {
        ClassResponse response = new ClassResponse();
        response.setId(clazz.getId());
        response.setName(clazz.getName());
        response.setGradeId(clazz.getGradeId());
        response.setGradeName(gradeName);
        response.setGraduated(clazz.getGraduated());
        response.setStudentCount(studentCount);
        return response;
    }

    private SchoolYearResponse toSchoolYearResponse(SchoolYear year) {
        return new SchoolYearResponse(year.getId(), year.getName(), year.getStartDate(),
                year.getEndDate(), year.getCurrent());
    }

    private List<StudentResponse> toStudentResponses(List<Student> students) {
        if (students.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = students.stream().map(Student::getUserId).collect(Collectors.toSet());
        Set<Long> classIds = students.stream().map(Student::getClassId).collect(Collectors.toSet());

        Map<Long, SysUser> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        Map<Long, SchoolClass> classes = classRepository.findAllById(classIds).stream()
                .collect(Collectors.toMap(SchoolClass::getId, c -> c));

        Set<Long> gradeIds = new HashSet<>();
        classes.values().forEach(c -> gradeIds.add(c.getGradeId()));
        Map<Long, String> gradeNames = gradeRepository.findAllById(gradeIds).stream()
                .collect(Collectors.toMap(Grade::getId, Grade::getName));

        List<StudentResponse> result = new ArrayList<>(students.size());
        for (Student student : students) {
            StudentResponse response = new StudentResponse();
            response.setId(student.getId());
            response.setUserId(student.getUserId());
            response.setStudentNo(student.getStudentNo());
            response.setClassId(student.getClassId());
            response.setEnrollYear(student.getEnrollYear());

            SysUser user = users.get(student.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setRealName(user.getRealName());
                response.setStatus(user.getStatus());
            }

            SchoolClass clazz = classes.get(student.getClassId());
            if (clazz != null) {
                response.setClassName(clazz.getName());
                response.setGradeId(clazz.getGradeId());
                response.setGradeName(gradeNames.get(clazz.getGradeId()));
            }
            result.add(response);
        }
        return result;
    }

    /**
     * 生成学号：入学年份 + 4 位顺序号，如 20260001
     */
    private String generateStudentNo(Integer enrollYear) {
        String prefix = String.valueOf(enrollYear);
        String max = studentRepository.findMaxStudentNoByPrefix(prefix);
        int sequence = 1;
        if (max != null && max.length() > prefix.length()) {
            try {
                sequence = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                sequence = 1;
            }
        }
        return prefix + String.format("%04d", sequence);
    }
}
