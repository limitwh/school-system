package com.school.repository;

import com.school.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 年级数据访问层
 */
@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findAllByOrderBySortOrderAsc();

    Optional<Grade> findByName(String name);
}
