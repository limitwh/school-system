package com.school.controller.admin;

import com.school.common.Result;
import com.school.dto.ArchiveResult;
import com.school.dto.OperationLogResponse;
import com.school.dto.PageResult;
import com.school.service.LogArchiveService;
import com.school.service.OperationLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 管理员 - 操作日志查询接口（仅系统管理员 ADMIN 可访问）
 */
@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
public class OperationLogController {

    private final OperationLogService operationLogService;
    private final LogArchiveService logArchiveService;

    public OperationLogController(OperationLogService operationLogService,
                                  LogArchiveService logArchiveService) {
        this.operationLogService = operationLogService;
        this.logArchiveService = logArchiveService;
    }

    /**
     * 分页查询操作日志，支持按操作人、模块、操作类型、结果、时间范围过滤
     */
    @GetMapping
    public Result<PageResult<OperationLogResponse>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(operationLogService.page(page, size, username, module, action,
                success, startTime, endTime));
    }

    /**
     * 手动触发一次日志归档：把超过保留期的日志导出为文件并从库中删除
     */
    @PostMapping("/archive")
    public Result<ArchiveResult> archive() {
        return Result.success(logArchiveService.archiveNow(true));
    }
}
