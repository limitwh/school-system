package com.school.dto;

import java.time.LocalDateTime;

/**
 * 操作日志归档结果
 */
public class ArchiveResult {

    /** 本次归档写入文件的条数 */
    private long archivedCount;
    /** 归档文件路径（无需归档时为 null） */
    private String filePath;
    /** 从数据库删除的条数 */
    private int deletedCount;
    /** 执行时间 */
    private LocalDateTime executedAt;

    public ArchiveResult() {
    }

    public ArchiveResult(long archivedCount, String filePath, int deletedCount, LocalDateTime executedAt) {
        this.archivedCount = archivedCount;
        this.filePath = filePath;
        this.deletedCount = deletedCount;
        this.executedAt = executedAt;
    }

    public long getArchivedCount() {
        return archivedCount;
    }

    public void setArchivedCount(long archivedCount) {
        this.archivedCount = archivedCount;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getDeletedCount() {
        return deletedCount;
    }

    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}
