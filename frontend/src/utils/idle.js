/**
 * 空闲检测工具：记录最近一次用户操作时间，供「无操作自动退出」使用。
 * 时间戳存放在 localStorage，多个标签页共享同一份，避免一个标签页活跃、另一个标签页误退出。
 */

/** 无操作自动退出时长（毫秒）：默认 5 分钟 */
export const IDLE_TIMEOUT_MS = 5 * 60 * 1000

/** localStorage 中保存最近操作时间的键 */
export const ACTIVITY_KEY = 'school_system_last_activity'

/** 标记一次用户操作 */
export function markActivity() {
  try {
    localStorage.setItem(ACTIVITY_KEY, String(Date.now()))
  } catch {
    // 隐私模式等场景下存储不可用，忽略
  }
}

/** 清除操作时间记录（退出登录时调用） */
export function clearActivity() {
  try {
    localStorage.removeItem(ACTIVITY_KEY)
  } catch {
    // 忽略
  }
}

/** 距离最近一次操作已经过去的毫秒数；无记录时返回 0 */
export function getIdleMillis() {
  try {
    const raw = Number(localStorage.getItem(ACTIVITY_KEY))
    if (!raw) return 0
    return Date.now() - raw
  } catch {
    return 0
  }
}
