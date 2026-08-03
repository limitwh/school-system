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
