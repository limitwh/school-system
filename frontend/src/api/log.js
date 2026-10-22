import request from './request'

/**
 * 分页查询操作日志（仅管理员）
 * @param {object} params { page, size, username, module, action, success, startTime, endTime }
 * @returns {Promise<{total: number, page: number, size: number, records: Array}>}
 */
export function getOperationLogs(params) {
  return request.get('/admin/logs', { params })
}

/**
 * 手动触发日志归档：把超过保留期的日志导出为文件并从库中删除
 * @returns {Promise<{archivedCount: number, filePath: string|null, deletedCount: number, executedAt: string}>}
 */
export function archiveLogs() {
  return request.post('/admin/logs/archive')
}
