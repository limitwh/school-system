package com.school.repository;

import com.school.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学生数据访问层
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    long countByClassId(Long classId);

    List<Student> findByClassId(Long classId);

    boolean existsByStudentNo(String studentNo);

    /** 每个班级的学生数（一次查询） */
    @Query("select s.classId, count(s) from Student s group by s.classId")
    List<Object[]> countGroupByClass();

    /** 某前缀下最大的学号（用于生成顺序号） */
    @Query("select max(s.studentNo) from Student s where s.studentNo like concat(:prefix, '%')")
    String findMaxStudentNoByPrefix(@Param("prefix") String prefix);

    /** 分页查询学生：支持按班级、姓名/学号关键字过滤 */
    @Query("select s from Student s, SysUser u where s.userId = u.id " +
            "and (:classId is null or s.classId = :classId) " +
            "and (:keyword is null " +
            "     or lower(u.realName) like lower(concat('%', cast(:keyword as string), '%')) " +
            "     or lower(u.username) like lower(concat('%', cast(:keyword as string), '%')) " +
            "     or lower(s.studentNo) like lower(concat('%', cast(:keyword as string), '%'))) " +
            "order by s.id desc")
    Page<Student> search(@Param("keyword") String keyword,
                         @Param("classId") Long classId,
                         Pageable pageable);
}
