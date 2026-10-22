import request from './request'

/**
 * 登录
 * @param {{username: string, password: string}} data
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 获取当前登录用户信息
 */
export function getMe() {
  return request.get('/auth/me')
}

/**
 * 初始状态用户设置初始密码（成功后状态置为正常）
 * @param {{newPassword: string}} data
 */
export function setPassword(data) {
  return request.post('/auth/set-password', data)
}

/**
 * 修改密码（含密码过期后的强制修改）
 * @param {{oldPassword: string, newPassword: string}} data
 */
export function changePassword(data) {
  return request.post('/auth/change-password', data)
}

/**
 * 退出登录：仅用于后端记录操作日志（JWT 无状态）
 * @param {string} [token] 显式传入 Token（本地登录态可能已清除）
 * @param {'MANUAL'|'IDLE'} [reason] 退出原因：手动 / 无操作超时
 */
export function logout(token, reason = 'MANUAL') {
  const config = token ? { headers: { Authorization: `Bearer ${token}` } } : undefined
  return request.post('/auth/logout', { reason }, config)
}
