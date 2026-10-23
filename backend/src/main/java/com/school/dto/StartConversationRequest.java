package com.school.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 发起单聊请求
 */
public class StartConversationRequest {

    @NotNull(message = "请选择聊天对象")
    private Long peerUserId;

    public Long getPeerUserId() {
        return peerUserId;
    }

    public void setPeerUserId(Long peerUserId) {
        this.peerUserId = peerUserId;
    }
}
