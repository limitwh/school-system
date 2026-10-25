package com.school.repository;

import com.school.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 科目数据访问层
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findAllByOrderBySortOrderAsc();

    Optional<Subject> findByCode(String code);

    boolean existsByCode(String code);
}
