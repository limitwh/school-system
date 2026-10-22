package com.school.service.impl;

import com.school.entity.SysOperationLog;
import com.school.repository.SysOperationLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志写入器。
 * <p>
 * 使用独立事务（REQUIRES_NEW）写入：即使业务操作因异常回滚，失败日志也能保留。
 */
@Component
public class OperationLogWriter {

    private final SysOperationLogRepository repository;

    public OperationLogWriter(SysOperationLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(SysOperationLog operationLog) {
        repository.save(operationLog);
    }
}
