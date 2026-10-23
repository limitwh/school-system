import { defineStore } from 'pinia'
import { getUnreadCount } from '@/api/message'

/**
 * 消息未读状态：全局角标使用
 */
export const useMessageStore = defineStore('message', {
  state: () => ({
    unreadTotal: 0
  }),
  actions: {
    async refreshUnread() {
      try {
        const data = await getUnreadCount()
        this.unreadTotal = data.total || 0
      } catch {
        // 未登录或网络异常时忽略，不影响页面
      }
    },
    reset() {
      this.unreadTotal = 0
    }
  }
})
