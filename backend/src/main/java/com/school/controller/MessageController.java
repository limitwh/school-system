package com.school.controller;

import com.school.common.Result;
import com.school.dto.ConversationSummary;
import com.school.dto.MessageResponse;
import com.school.dto.SendMessageRequest;
import com.school.dto.StartConversationRequest;
import com.school.dto.UnreadCountResponse;
import com.school.dto.UserBrief;
import com.school.security.LoginUser;
import com.school.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息接口（所有登录用户可用：1:1 单聊）
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 我的会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ConversationSummary>> listConversations(Authentication authentication) {
        return Result.success(messageService.listConversations(currentUserId(authentication)));
    }

    /**
     * 与某人开始单聊（已存在则复用）
     */
    @PostMapping("/conversations")
    public Result<ConversationSummary> startConversation(Authentication authentication,
                                                         @RequestBody @Valid StartConversationRequest request) {
        return Result.success(messageService.startConversation(
                currentUserId(authentication), request.getPeerUserId()));
    }

    /**
     * 会话消息：beforeId 向上翻页，afterId 轮询增量，都不传则取最新一页
     */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<MessageResponse>> listMessages(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(required = false) Long afterId,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.listMessages(
                currentUserId(authentication), id, beforeId, afterId, size));
    }

    /**
     * 发送消息
     */
    @PostMapping("/conversations/{id}/messages")
    public Result<MessageResponse> sendMessage(Authentication authentication,
                                               @PathVariable Long id,
                                               @RequestBody @Valid SendMessageRequest request) {
        return Result.success(messageService.sendMessage(
                currentUserId(authentication), id, request.getContent()));
    }

    /**
     * 标记会话为已读
     */
    @PostMapping("/conversations/{id}/read")
    public Result<Void> markRead(Authentication authentication, @PathVariable Long id) {
        messageService.markRead(currentUserId(authentication), id);
        return Result.success();
    }

    /**
     * 未读总数（顶部角标）
     */
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> unreadCount(Authentication authentication) {
        return Result.success(new UnreadCountResponse(
                messageService.unreadCount(currentUserId(authentication))));
    }

    /**
     * 可发消息的用户（排除自己，支持关键字搜索）
     */
    @GetMapping("/contacts")
    public Result<List<UserBrief>> contacts(Authentication authentication,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(messageService.contacts(currentUserId(authentication), keyword));
    }

    private Long currentUserId(Authentication authentication) {
        return ((LoginUser) authentication.getPrincipal()).getUserId();
    }
}
