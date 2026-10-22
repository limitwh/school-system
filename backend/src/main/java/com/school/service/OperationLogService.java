package com.school.service;

import com.school.dto.OperationLogResponse;
import com.school.dto.PageResult;

import java.time.LocalDateTime;

/**
 * 操作日志服务
 */
public interface OperationLogService {

    /**
     * 记录登录事件（成功或失败）
     *
     * @param userId   用户 ID（账号不存在时为空）
     * @param username 登录名（失败时记录尝试登录的账号）
     * @param realName 姓名（可空）
     * @param success  是否成功
     * @param detail   成功说明或失败原因
     */
    void logLogin(Long userId, String username, String realName, boolean success, String detail);

    /**
     * 以当前登录用户身份记录一条操作日志
     */
    void logCurrent(String module, String action, String detail, boolean success);

    /**
     * 以系统身份记录一条操作日志（无登录用户的场景，如定时任务）
     */
    void logSystem(String module, String action, String detail, boolean success);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLogResponse> page(int page, int size, String username, String module,
                                          String action, Boolean success,
                                          LocalDateTime startTime, LocalDateTime endTime);
}
