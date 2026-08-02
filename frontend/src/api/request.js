import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

// 统一请求实例：baseURL=/api，经 Vite 代理到后端
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 Token
request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：后端统一返回 Result { code, message, data }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 423 = 账号锁定（密码连续错误）
    if (res.code === 423) {
      ElMessage.error(res.message || '账号已被锁定，请联系管理员')
    } else {
      ElMessage.error(res.message || '请求失败，请稍后重试')
    }
    return Promise.reject(new Error(res.message || 'Request Error'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status === 401) {
      removeToken()
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    } else if (status === 403) {
      ElMessage.error(msg || '没有访问权限')
    } else if (msg) {
      ElMessage.error(msg)
    } else {
      ElMessage.error('网络异常，请检查后端服务是否启动')
    }
    return Promise.reject(error)
  }
)

export default request
