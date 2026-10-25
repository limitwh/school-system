package com.school.repository;

import com.school.entity.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 节次作息数据访问层
 */
@Repository
public interface PeriodRepository extends JpaRepository<Period, Long> {

    List<Period> findByStageOrderByPeriodNoAsc(String stage);

    long countByStage(String stage);
}
