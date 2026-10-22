package com.school.repository;

import com.school.entity.SysOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 操作日志数据访问层
 */
@Repository
public interface SysOperationLogRepository
        extends JpaRepository<SysOperationLog, Long>, JpaSpecificationExecutor<SysOperationLog> {

    /** 统计早于指定时间的日志条数（归档前判断） */
    long countByCreatedAtBefore(LocalDateTime cutoff);

    /** 分页查询早于指定时间的日志（归档导出用） */
    Page<SysOperationLog> findByCreatedAtBefore(LocalDateTime cutoff, Pageable pageable);

    /** 删除早于指定时间的日志，返回删除条数（归档后清理用） */
    @Modifying
    @Query("delete from SysOperationLog l where l.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
