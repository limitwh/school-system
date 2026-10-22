package com.school.job;

import com.school.dto.ArchiveResult;
import com.school.service.LogArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 操作日志归档定时任务。
 * <p>
 * 默认每天 03:00 执行，可通过 app.log.archive.cron 配置。
 * 把超过保留期的日志导出为 JSONL 文件，再从数据库中删除。
 */
@Component
public class OperationLogArchiveJob {

    private static final Logger log = LoggerFactory.getLogger(OperationLogArchiveJob.class);

    private final LogArchiveService logArchiveService;

    public OperationLogArchiveJob(LogArchiveService logArchiveService) {
        this.logArchiveService = logArchiveService;
    }

    @Scheduled(cron = "${app.log.archive.cron:0 0 3 * * ?}")
    public void archiveDaily() {
        try {
            ArchiveResult result = logArchiveService.archiveNow(false);
            if (result.getArchivedCount() > 0) {
                log.info("操作日志归档任务完成：导出 {} 条，删除 {} 条，文件 {}",
                        result.getArchivedCount(), result.getDeletedCount(), result.getFilePath());
            }
        } catch (Exception e) {
            log.error("操作日志归档任务失败：{}", e.getMessage(), e);
        }
    }
}
