import { defineStore } from 'pinia'
import { login as loginApi, getMe, logout as logoutApi } from '@/api/auth'
import { getToken, setToken, removeToken, getUser, setUser } from '@/utils/auth'
import { clearActivity, markActivity } from '@/utils/idle'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUser() || null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    roles: (state) => state.userInfo?.roles || []
  },
  actions: {
    async login(loginForm) {
      const data = await loginApi(loginForm)
      this.token = data.token
      setToken(data.token)
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        status: data.status,
        roles: data.roles
      }
      setUser(this.userInfo)
      // 登录成功即开始计算空闲时间
      markActivity()
      return data
    },
    async fetchMe() {
      const data = await getMe()
      this.userInfo = data
      setUser(data)
      return data
    },
    // 初始密码设置成功后，将状态标记为「正常」
    markNormal() {
      if (this.userInfo) {
        this.userInfo.status = 1
        setUser(this.userInfo)
      }
    },
    /**
     * 退出登录
     * @param {'MANUAL'|'IDLE'} [reason] 退出原因：手动 / 无操作超时
     */
    logout(reason = 'MANUAL') {
      const token = this.token
      this.token = ''
      this.userInfo = null
      removeToken()
      clearActivity()
      // 异步补一条退出日志，失败忽略（JWT 无状态，不影响本地登出）
      logoutApi(token, reason).catch(() => {})
    }
  }
})
