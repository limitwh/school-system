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
