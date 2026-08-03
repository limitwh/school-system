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
