<template>
  <div class="message-page">
    <el-container class="container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon :size="22" color="#1f6feb"><School /></el-icon>
          <span class="logo-text">校务管理系统</span>
        </div>
        <div class="header-right">
          <el-button text @click="$router.push('/')">返回首页</el-button>
          <MessageBell />
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="avatar">{{ avatarText }}</el-avatar>
              <span class="username">
                {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
              </span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="change-password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <div class="chat-layout">
          <!-- 左侧会话列表 -->
          <aside class="conversation-pane">
            <div class="pane-header">
              <span class="pane-title">消息</span>
              <el-button size="small" type="primary" @click="dialogVisible = true">
                <el-icon style="margin-right: 4px"><Plus /></el-icon>
                新建
              </el-button>
            </div>

            <el-scrollbar class="conversation-scroll">
              <div
                v-for="c in conversations"
                :key="c.id"
                class="conversation-item"
                :class="{ active: c.id === activeId }"
                @click="openConversation(c)"
              >
                <el-avatar :size="38" class="conv-avatar">{{ initialOf(c.peer) }}</el-avatar>
                <div class="conv-main">
                  <div class="conv-line">
                    <span class="conv-name">{{ peerName(c.peer) }}</span>
                    <span class="conv-time">{{ formatShort(c.lastMessageTime) }}</span>
                  </div>
                  <div class="conv-line">
                    <span class="conv-preview">{{ c.lastMessage }}</span>
                    <el-badge
                      v-if="c.unreadCount > 0"
                      :value="c.unreadCount"
                      :max="99"
                      class="conv-badge"
                    />
                  </div>
                </div>
              </div>

              <el-empty v-if="!conversations.length" description="暂无会话" :image-size="72" />
            </el-scrollbar>
          </aside>

          <!-- 右侧聊天区 -->
          <section class="chat-pane">
            <template v-if="activeConversation">
              <div class="chat-header">
                <span class="chat-title">{{ peerName(activeConversation.peer) }}</span>
                <span class="chat-sub">{{ activeConversation.peer?.username }}</span>
              </div>

              <div ref="chatBodyRef" class="chat-body">
                <div v-if="hasMore" class="load-more" @click="loadEarlier">
                  {{ loadingEarlier ? '加载中...' : '查看更早的消息' }}
                </div>

                <div
                  v-for="m in messages"
                  :key="m.id"
                  class="message-row"
                  :class="{ mine: isMine(m) }"
                >
                  <div v-if="!isMine(m)" class="message-sender">{{ m.senderName }}</div>
                  <div class="bubble">{{ m.content }}</div>
                  <div class="message-time">{{ formatTime(m.createdAt) }}</div>
                </div>

                <el-empty
                  v-if="!messages.length"
                  description="还没有消息，打个招呼吧"
                  :image-size="72"
                />
              </div>

              <div class="chat-input">
                <el-input
                  v-model="input"
                  type="textarea"
                  :rows="3"
                  resize="none"
                  placeholder="输入消息，Enter 发送，Shift+Enter 换行"
                  @keydown.enter="handleEnter"
                />
                <div class="input-actions">
                  <el-button
                    type="primary"
                    :loading="sending"
                    :disabled="!input.trim()"
                    @click="handleSend"
                  >
                    发送
                  </el-button>
                </div>
              </div>
            </template>

            <el-empty v-else description="选择左侧会话开始聊天" />
          </section>
        </div>
      </el-main>
    </el-container>

    <NewConversationDialog v-model="dialogVisible" @select="handleStartConversation" />
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Plus, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useMessageStore } from '@/store/message'
import {
  getConversations,
  getMessages,
  markConversationRead,
  sendMessage,
  startConversation
} from '@/api/message'
import NewConversationDialog from '@/components/message/NewConversationDialog.vue'
import MessageBell from '@/components/common/MessageBell.vue'

const router = useRouter()
const userStore = useUserStore()
const messageStore = useMessageStore()

const myId = computed(() => userStore.userInfo?.userId)

const conversations = ref([])
const activeId = ref(null)
const messages = ref([])
const input = ref('')
const sending = ref(false)
const hasMore = ref(false)
const loadingEarlier = ref(false)
const dialogVisible = ref(false)
const chatBodyRef = ref(null)

const activeConversation = computed(
  () => conversations.value.find((c) => c.id === activeId.value) || null
)

const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

let conversationTimer = null
let messageTimer = null

/* ---------- 展示辅助 ---------- */

function peerName(peer) {
  return peer?.realName || peer?.username || '未知用户'
}

function initialOf(peer) {
  return peerName(peer).charAt(0).toUpperCase()
}

function isMine(message) {
  return message.senderId === myId.value
}

function pad(n) {
  return String(n).padStart(2, '0')
}

function formatShort(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const now = new Date()
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
  return sameDay ? `${pad(d.getHours())}:${pad(d.getMinutes())}` : `${d.getMonth() + 1}-${d.getDate()}`
}

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  return `${d.getMonth() + 1}-${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/* ---------- 数据加载 ---------- */

async function loadConversations() {
  try {
    conversations.value = await getConversations()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
}

async function openConversation(conversation) {
  if (activeId.value === conversation.id) return

  activeId.value = conversation.id
  conversation.unreadCount = 0
  messages.value = []
  hasMore.value = false

  try {
    const list = await getMessages(conversation.id, { size: 20 })
    messages.value = list
    hasMore.value = list.length === 20
    await scrollToBottom()
    await markConversationRead(conversation.id).catch(() => {})
    messageStore.refreshUnread()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
}

async function loadEarlier() {
  if (!activeId.value || !messages.value.length || loadingEarlier.value) return

  loadingEarlier.value = true
  const body = chatBodyRef.value
  const beforeHeight = body ? body.scrollHeight : 0

  try {
    const list = await getMessages(activeId.value, { beforeId: messages.value[0].id, size: 20 })
    messages.value = [...list, ...messages.value]
    hasMore.value = list.length === 20
    await nextTick()
    if (body) {
      body.scrollTop = body.scrollHeight - beforeHeight
    }
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loadingEarlier.value = false
  }
}

async function pollActiveConversation() {
  if (!activeId.value || document.visibilityState !== 'visible') return

  const lastId = messages.value.length ? messages.value[messages.value.length - 1].id : 0

  try {
    const list = await getMessages(activeId.value, { afterId: lastId })
    if (!list.length) return

    messages.value = [...messages.value, ...list]
    await scrollToBottom()
    await markConversationRead(activeId.value).catch(() => {})
    messageStore.refreshUnread()
    await loadConversations()
  } catch (e) {
    // 轮询失败静默处理，下一轮重试
  }
}

async function scrollToBottom() {
  await nextTick()
  const body = chatBodyRef.value
  if (body) {
    body.scrollTop = body.scrollHeight
  }
}

/* ---------- 交互 ---------- */

async function handleSend() {
  const content = input.value.trim()
  if (!content || sending.value || !activeId.value) return

  sending.value = true
  try {
    const message = await sendMessage(activeId.value, content)
    messages.value = [...messages.value, message]
    input.value = ''
    await scrollToBottom()
    await loadConversations()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    sending.value = false
  }
}

function handleEnter(event) {
  // Shift+Enter 换行；输入法组合中不发送
  if (event.shiftKey || event.isComposing) return
  event.preventDefault()
  handleSend()
}

async function handleStartConversation(userId) {
  try {
    const summary = await startConversation(userId)
    const existing = conversations.value.find((c) => c.id === summary.id)
    if (existing) {
      await openConversation(existing)
    } else {
      conversations.value = [summary, ...conversations.value]
      await openConversation(summary)
    }
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
}

function handleCommand(command) {
  if (command === 'change-password') {
    router.push('/change-password')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
}

onMounted(async () => {
  await loadConversations()
  conversationTimer = window.setInterval(loadConversations, 30000)
  messageTimer = window.setInterval(pollActiveConversation, 5000)
})

onBeforeUnmount(() => {
  if (conversationTimer) window.clearInterval(conversationTimer)
  if (messageTimer) window.clearInterval(messageTimer)
})
</script>

<style scoped>
.message-page {
  height: 100%;
}

.container {
  height: 100%;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1f6feb;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
  outline: none;
}

.avatar {
  background: #1f6feb;
  color: #fff;
  font-weight: 600;
}

.main {
  background-color: #f5f7fa;
  padding: 16px;
  overflow: hidden;
}

.chat-layout {
  display: flex;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 左侧会话列表 */
.conversation-pane {
  width: 280px;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}

.pane-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
}

.pane-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.conversation-scroll {
  flex: 1;
  overflow: hidden;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f7fa;
}

.conversation-item:hover {
  background: #f7f9fc;
}

.conversation-item.active {
  background: #e8f1fe;
}

.conv-avatar {
  background: #1f6feb;
  color: #fff;
  flex-shrink: 0;
}

.conv-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conv-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.conv-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-time {
  font-size: 12px;
  color: #c0c4cc;
  flex-shrink: 0;
}

.conv-preview {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 右侧聊天区 */
.chat-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 14px 20px;
  border-bottom: 1px solid #ebeef5;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chat-sub {
  font-size: 12px;
  color: #909399;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #fafbfc;
}

.load-more {
  text-align: center;
  font-size: 12px;
  color: #1f6feb;
  cursor: pointer;
  padding: 6px 0 12px;
}

.message-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 14px;
}

.message-row.mine {
  align-items: flex-end;
}

.message-sender {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.bubble {
  max-width: 70%;
  padding: 9px 13px;
  border-radius: 10px;
  background: #fff;
  color: #303133;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.message-row.mine .bubble {
  background: #1f6feb;
  color: #fff;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
  margin-top: 4px;
}

.chat-input {
  border-top: 1px solid #ebeef5;
  padding: 12px 20px 16px;
  background: #fff;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
