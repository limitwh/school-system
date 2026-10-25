package com.school.repository;

import com.school.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 课表条目数据访问层
 */
@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByTimetableId(Long timetableId);

    void deleteByTimetableId(Long timetableId);
}
