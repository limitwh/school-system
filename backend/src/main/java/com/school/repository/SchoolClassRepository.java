package com.school.repository;

import com.school.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 班级数据访问层
 */
@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findAllByOrderByIdAsc();

    List<SchoolClass> findByGradeIdOrderByNameAsc(Long gradeId);

    boolean existsByGradeIdAndName(Long gradeId, String name);

    boolean existsByGradeIdAndNameAndIdNot(Long gradeId, String name, Long id);
}
