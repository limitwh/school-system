import request from './request'

/**
 * 查询所有锁定状态的用户
 */
export function getLockedUsers() {
  return request.get('/admin/users/locked')
}

/**
 * 重置锁定用户密码：状态置为初始(0)并分配初始密码
 * @param {number} id 用户 ID
 * @param {{newPassword: string}} data 初始密码
 */
export function resetUserPassword(id, data) {
  return request.post(`/admin/users/${id}/reset-password`, data)
}

/**
 * 按登录名重置用户密码：生成一次性随机密码，状态置为初始(0)
 * @param {{loginName: string}} data 登录名（用户名 / 学号 / 职工号）
 * @returns {Promise<{username: string, loginNo: string, realName: string, oneTimePassword: string, validHours: number}>}
 */
export function resetPasswordByLoginName(data) {
  return request.post('/admin/users/reset-password', data)
}
