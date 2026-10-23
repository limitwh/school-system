package com.school.repository;

import com.school.entity.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学年数据访问层
 */
@Repository
public interface SchoolYearRepository extends JpaRepository<SchoolYear, Long> {

    List<SchoolYear> findAllByOrderByStartDateDesc();

    Optional<SchoolYear> findByCurrentTrue();
}
