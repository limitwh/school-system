package com.school.service;

import com.school.dto.ArchiveResult;

/**
 * 操作日志归档服务：把超过保留期的日志导出为文件，再从数据库中删除。
 */
public interface LogArchiveService {

    /**
     * 立即执行一次归档。
     *
     * @param manual true=管理员手动触发；false=定时任务触发（影响日志记录方式）
     * @return 归档结果
     */
    ArchiveResult archiveNow(boolean manual);
}
