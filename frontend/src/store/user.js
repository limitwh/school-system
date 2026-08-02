import { defineStore } from 'pinia'
import { login as loginApi, getMe } from '@/api/auth'
import { getToken, setToken, removeToken, getUser, setUser } from '@/utils/auth'

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
        roles: data.roles
      }
      setUser(this.userInfo)
      return data
    },
    async fetchMe() {
      const data = await getMe()
      this.userInfo = data
      setUser(data)
      return data
    },
    logout() {
      this.token = ''
      this.userInfo = null
      removeToken()
    }
  }
})
