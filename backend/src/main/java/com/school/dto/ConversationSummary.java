package com.school.dto;

import java.time.LocalDateTime;

/**
 * 会话列表项
 */
public class ConversationSummary {

    private Long id;
    /** 单聊对方 */
    private UserBrief peer;
    /** 最后一条消息内容（无消息时为空） */
    private String lastMessage;
    /** 最后一条消息的发送人 */
    private Long lastSenderId;
    /** 最后一条消息时间 */
    private LocalDateTime lastMessageTime;
    /** 当前用户未读数 */
    private long unreadCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserBrief getPeer() {
        return peer;
    }

    public void setPeer(UserBrief peer) {
        this.peer = peer;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(Long lastSenderId) {
        this.lastSenderId = lastSenderId;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
