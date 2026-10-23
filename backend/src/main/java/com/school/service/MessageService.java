package com.school.service;

import com.school.dto.ConversationSummary;
import com.school.dto.MessageResponse;
import com.school.dto.UserBrief;

import java.util.List;

/**
 * 消息服务（1:1 单聊）
 */
public interface MessageService {

    /** 我的会话列表（含对方、最后一条消息、未读数） */
    List<ConversationSummary> listConversations(Long userId);

    /** 与某人开始单聊；已存在则复用 */
    ConversationSummary startConversation(Long userId, Long peerUserId);

    /**
     * 会话消息
     *
     * @param beforeId 加载更早的消息（向上翻页）
     * @param afterId  拉取更新的消息（轮询增量）
     */
    List<MessageResponse> listMessages(Long userId, Long conversationId, Long beforeId, Long afterId, int size);

    /** 发送消息 */
    MessageResponse sendMessage(Long userId, Long conversationId, String content);

    /** 把会话标记为已读 */
    void markRead(Long userId, Long conversationId);

    /** 未读总数 */
    long unreadCount(Long userId);

    /** 可发消息的用户（排除自己） */
    List<UserBrief> contacts(Long userId, String keyword);
}
