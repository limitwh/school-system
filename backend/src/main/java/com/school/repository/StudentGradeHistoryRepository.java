package com.school.repository;

import com.school.entity.StudentGradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学生年级变更历史数据访问层
 */
@Repository
public interface StudentGradeHistoryRepository extends JpaRepository<StudentGradeHistory, Long> {

    List<StudentGradeHistory> findByStudentIdOrderByIdDesc(Long studentId);

    /** 判断某学年是否已执行过某类批量操作（防重复执行） */
    boolean existsBySchoolYearIdAndChangeType(Long schoolYearId, String changeType);
}
