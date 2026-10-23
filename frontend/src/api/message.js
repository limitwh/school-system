import request from './request'

/** 我的会话列表 */
export function getConversations() {
  return request.get('/messages/conversations')
}

/** 与某人开始单聊（已存在则复用） */
export function startConversation(peerUserId) {
  return request.post('/messages/conversations', { peerUserId })
}

/**
 * 会话消息
 * @param {number} conversationId
 * @param {{beforeId?: number, afterId?: number, size?: number}} params
 */
export function getMessages(conversationId, params = {}) {
  return request.get(`/messages/conversations/${conversationId}/messages`, { params })
}

/** 发送消息 */
export function sendMessage(conversationId, content) {
  return request.post(`/messages/conversations/${conversationId}/messages`, { content })
}

/** 标记会话已读 */
export function markConversationRead(conversationId) {
  return request.post(`/messages/conversations/${conversationId}/read`)
}

/** 未读总数 */
export function getUnreadCount() {
  return request.get('/messages/unread-count')
}

/** 可发消息的用户 */
export function getContacts(keyword) {
  return request.get('/messages/contacts', { params: { keyword } })
}
