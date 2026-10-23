package com.school.service;

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

import java.util.List;

/**
 * 学籍基础数据服务：年级、班级、学生、学年
 */
public interface AcademicService {

    /** 年级列表（只读，来自种子数据） */
    List<GradeResponse> listGrades();

    /** 班级列表（含学生数） */
    List<ClassResponse> listClasses();

    ClassResponse createClass(ClassRequest request);

    ClassResponse updateClass(Long id, ClassRequest request);

    /** 学生分页查询 */
    PageResult<StudentResponse> listStudents(int page, int size, String keyword, Long classId);

    /** 新增学生：同时创建登录账号（初始密码，首次登录强制修改） */
    StudentResponse createStudent(StudentRequest request, Long operatorId);

    /** 编辑学生：可改姓名、班级、入学年份；班级变化会记录历史 */
    StudentResponse updateStudent(Long id, StudentRequest request, Long operatorId);

    List<SchoolYearResponse> listSchoolYears();

    SchoolYearResponse createSchoolYear(SchoolYearRequest request);

    SchoolYearResponse setCurrentSchoolYear(Long id);

    /** 升级/毕业预览：按当前学年统计待升级与待毕业的班级和学生 */
    PromotionPreview previewPromotion();

    /** 执行升级/毕业：班级整体升级 + 初三毕业归档（同一学年只执行一次） */
    PromotionResult executePromotion(Long operatorId);
}
