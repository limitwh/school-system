package com.school.controller.admin;

import com.school.common.Result;
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
import com.school.security.LoginUser;
import com.school.service.AcademicService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员 - 学籍基础数据（年级 / 班级 / 学生 / 学年）
 */
@RestController
@RequestMapping("/api/admin/academic")
@PreAuthorize("hasRole('ADMIN')")
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    /* ================= 年级 ================= */

    @GetMapping("/grades")
    public Result<List<GradeResponse>> listGrades() {
        return Result.success(academicService.listGrades());
    }

    /* ================= 班级 ================= */

    @GetMapping("/classes")
    public Result<List<ClassResponse>> listClasses() {
        return Result.success(academicService.listClasses());
    }

    @PostMapping("/classes")
    public Result<ClassResponse> createClass(@RequestBody @Valid ClassRequest request) {
        return Result.success(academicService.createClass(request));
    }

    @PutMapping("/classes/{id}")
    public Result<ClassResponse> updateClass(@PathVariable Long id,
                                             @RequestBody @Valid ClassRequest request) {
        return Result.success(academicService.updateClass(id, request));
    }

    /* ================= 学生 ================= */

    @GetMapping("/students")
    public Result<PageResult<StudentResponse>> listStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId) {
        return Result.success(academicService.listStudents(page, size, keyword, classId));
    }

    @PostMapping("/students")
    public Result<StudentResponse> createStudent(Authentication authentication,
                                                 @RequestBody @Valid StudentRequest request) {
        return Result.success(academicService.createStudent(request, currentUserId(authentication)));
    }

    @PutMapping("/students/{id}")
    public Result<StudentResponse> updateStudent(Authentication authentication,
                                                 @PathVariable Long id,
                                                 @RequestBody @Valid StudentRequest request) {
        return Result.success(academicService.updateStudent(id, request, currentUserId(authentication)));
    }

    /* ================= 学年 ================= */

    @GetMapping("/school-years")
    public Result<List<SchoolYearResponse>> listSchoolYears() {
        return Result.success(academicService.listSchoolYears());
    }

    @PostMapping("/school-years")
    public Result<SchoolYearResponse> createSchoolYear(@RequestBody @Valid SchoolYearRequest request) {
        return Result.success(academicService.createSchoolYear(request));
    }

    @PostMapping("/school-years/{id}/current")
    public Result<SchoolYearResponse> setCurrentSchoolYear(@PathVariable Long id) {
        return Result.success(academicService.setCurrentSchoolYear(id));
    }

    /* ================= 升级 / 毕业 ================= */

    @GetMapping("/promotion/preview")
    public Result<PromotionPreview> previewPromotion() {
        return Result.success(academicService.previewPromotion());
    }

    @PostMapping("/promotion/execute")
    public Result<PromotionResult> executePromotion(Authentication authentication) {
        return Result.success(academicService.executePromotion(currentUserId(authentication)));
    }

    private Long currentUserId(Authentication authentication) {
        return ((LoginUser) authentication.getPrincipal()).getUserId();
    }
}
