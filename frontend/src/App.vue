<template>
  <router-view />
</template>

<script setup>
import { onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useMessageStore } from '@/store/message'
import { IDLE_TIMEOUT_MS, clearActivity, getIdleMillis, markActivity } from '@/utils/idle'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()

/** 视为「用户有操作」的事件 */
const ACTIVITY_EVENTS = ['mousemove', 'mousedown', 'keydown', 'scroll', 'wheel', 'touchstart', 'click']

/** 空闲检查间隔（毫秒） */
const CHECK_INTERVAL_MS = 10000

const TOKEN_KEY = 'school_system_token'

let checkTimer = null
let unreadTimer = null
let lastMarkAt = 0
let loggingOut = false

/** 记录用户操作（节流：最多每秒写一次） */
function handleActivity() {
  if (!userStore.isLoggedIn) return
  const now = Date.now()
  if (now - lastMarkAt < 1000) return
  lastMarkAt = now
  markActivity()
}

/** 多标签页同步：其他标签页退出后，本标签页也清理登录态 */
function handleStorage(event) {
  if (event.key === TOKEN_KEY && !event.newValue) {
    userStore.token = ''
    userStore.userInfo = null
    if (router.currentRoute.value.meta?.requiresAuth) {
      router.push('/login')
    }
  }
}

/** 切回标签页时立即检查一次（后台标签页的定时器会被浏览器降频） */
function handleVisibility() {
  if (document.visibilityState === 'visible') {
    checkIdle()
    refreshUnread()
  }
}

/** 刷新全局未读数（顶部消息角标） */
function refreshUnread() {
  if (userStore.isLoggedIn) {
    messageStore.refreshUnread()
  }
}

/** 检查是否已超过空闲时限 */
function checkIdle() {
  if (!userStore.isLoggedIn || loggingOut) return

  const idle = getIdleMillis()
  if (idle === 0) {
    // 没有记录（例如刚登录）→ 以当前时间为起点
    markActivity()
    return
  }

  if (idle >= IDLE_TIMEOUT_MS) {
    autoLogout()
  }
}

/** 无操作超时：自动退出并回到登录页 */
function autoLogout() {
  if (loggingOut) return
  loggingOut = true

  const minutes = Math.round(IDLE_TIMEOUT_MS / 60000)
  ElMessage.warning(`您已超过 ${minutes} 分钟未操作，已自动退出登录`)

  userStore.logout('IDLE')
  clearActivity()

  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }

  // 允许此后的新会话重新计时
  window.setTimeout(() => {
    loggingOut = false
  }, 1000)
}

onMounted(() => {
  ACTIVITY_EVENTS.forEach((name) =>
    window.addEventListener(name, handleActivity, { passive: true })
  )
  window.addEventListener('storage', handleStorage)
  document.addEventListener('visibilitychange', handleVisibility)

  checkTimer = window.setInterval(checkIdle, CHECK_INTERVAL_MS)

  // 消息未读数定时刷新（30 秒）
  refreshUnread()
  unreadTimer = window.setInterval(refreshUnread, 30000)
})

onBeforeUnmount(() => {
  ACTIVITY_EVENTS.forEach((name) => window.removeEventListener(name, handleActivity))
  window.removeEventListener('storage', handleStorage)
  document.removeEventListener('visibilitychange', handleVisibility)
  if (checkTimer) {
    window.clearInterval(checkTimer)
  }
  if (unreadTimer) {
    window.clearInterval(unreadTimer)
  }
})

// 退出登录后清空未读角标
watch(
  () => userStore.token,
  (token) => {
    if (!token) {
      messageStore.reset()
    }
  }
)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
}

body {
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB",
    "Microsoft YaHei", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #303133;
  background-color: #f5f7fa;
}
</style>
