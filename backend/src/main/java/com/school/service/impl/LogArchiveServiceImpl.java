package com.school.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.BusinessException;
import com.school.dto.ArchiveResult;
import com.school.dto.OperationLogResponse;
import com.school.entity.SysOperationLog;
import com.school.repository.SysOperationLogRepository;
import com.school.service.LogArchiveService;
import com.school.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 操作日志归档服务实现。
 * <p>
 * 流程：导出超过保留期的日志为 JSONL 文件 → 校验写入完成 → 从数据库删除 → 记录一条归档日志。
 * 导出失败时不会删除任何数据。
 */
@Service
public class LogArchiveServiceImpl implements LogArchiveService {

    private static final Logger log = LoggerFactory.getLogger(LogArchiveServiceImpl.class);

    /** 每批读取条数，避免一次性加载过多数据 */
    private static final int BATCH_SIZE = 1000;

    private final SysOperationLogRepository repository;
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    /** 防止定时任务与手动触发并发执行 */
    private final ReentrantLock lock = new ReentrantLock();

    /** 归档文件目录（相对后端工作目录） */
    @Value("${app.log.archive.dir:logs/archive}")
    private String archiveDir;

    /** 日志保留天数，超过该天数的日志归档后删除 */
    @Value("${app.log.archive.retention-days:30}")
    private int retentionDays;

    public LogArchiveServiceImpl(SysOperationLogRepository repository,
                                 OperationLogService operationLogService,
                                 ObjectMapper objectMapper,
                                 PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public ArchiveResult archiveNow(boolean manual) {
        if (!lock.tryLock()) {
            throw new BusinessException(409, "日志归档任务正在执行中，请稍后再试");
        }
        try {
            return doArchive(manual);
        } finally {
            lock.unlock();
        }
    }

    private ArchiveResult doArchive(boolean manual) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(Math.max(retentionDays, 1));
        long total = repository.countByCreatedAtBefore(cutoff);

        if (total == 0) {
            log.info("操作日志无需归档：{} 天前的日志为空", retentionDays);
            return new ArchiveResult(0, null, 0, LocalDateTime.now());
        }

        Path target;
        long written = 0;

        try {
            Path dir = Paths.get(archiveDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
            String fileName = "operation-log-" + timestamp + ".jsonl";
            Path tempFile = dir.resolve(fileName + ".tmp");
            target = dir.resolve(fileName);

            // 先写临时文件，全部写完再改名，避免留下半截文件
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                int pageIndex = 0;
                while (true) {
                    Pageable pageable = PageRequest.of(pageIndex, BATCH_SIZE, Sort.by("id"));
                    Page<SysOperationLog> page = repository.findByCreatedAtBefore(cutoff, pageable);
                    if (page.isEmpty()) {
                        break;
                    }
                    for (SysOperationLog item : page.getContent()) {
                        writer.write(objectMapper.writeValueAsString(OperationLogResponse.from(item)));
                        writer.newLine();
                        written++;
                    }
                    if (!page.hasNext()) {
                        break;
                    }
                    pageIndex++;
                }
            }

            moveAtomically(tempFile, target);

            Integer deleted = transactionTemplate.execute(status -> repository.deleteByCreatedAtBefore(cutoff));
            int deletedCount = deleted == null ? 0 : deleted;

            String detail = "归档操作日志 " + written + " 条，清理 " + deletedCount + " 条 → " + target;
            if (manual) {
                operationLogService.logCurrent("SYSTEM", "ARCHIVE_LOG", detail, true);
            } else {
                operationLogService.logSystem("SYSTEM", "ARCHIVE_LOG", detail, true);
            }

            log.info("操作日志归档完成：{}", detail);
            return new ArchiveResult(written, target.toString(), deletedCount, LocalDateTime.now());
        } catch (IOException e) {
            throw new BusinessException(500, "日志归档失败：" + e.getMessage());
        }
    }

    /**
     * 原子移动文件；个别文件系统不支持时退化为普通替换
     */
    private void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
