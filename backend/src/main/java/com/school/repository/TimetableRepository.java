package com.school.repository;

import com.school.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 班级课表头数据访问层
 */
@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {

    Optional<Timetable> findByClassIdAndSchoolYearIdAndTerm(Long classId, Long schoolYearId, Integer term);
}
