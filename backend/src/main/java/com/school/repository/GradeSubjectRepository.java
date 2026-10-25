package com.school.repository;

import com.school.entity.GradeSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 年级课程计划数据访问层
 */
@Repository
public interface GradeSubjectRepository extends JpaRepository<GradeSubject, Long> {

    List<GradeSubject> findByGradeId(Long gradeId);

    void deleteByGradeId(Long gradeId);
}
