package com.school.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 消息会话，对应表 msg_conversation
 * <p>
 * 单聊（SINGLE）用 pair_key 去重；GROUP 类型为后续群聊预留。
 */
@Entity
@Table(name = "msg_conversation")
public class MsgConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会话类型：SINGLE 单聊 / GROUP 群聊（预留） */
    @Column(nullable = false, length = 10)
    private String type = "SINGLE";

    /** 群聊名称（单聊为空） */
    @Column(length = 100)
    private String title;

    /** 单聊去重键：小用户ID_大用户ID；群聊为空 */
    @Column(name = "pair_key", length = 50, unique = true)
    private String pairKey;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPairKey() {
        return pairKey;
    }

    public void setPairKey(String pairKey) {
        this.pairKey = pairKey;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
