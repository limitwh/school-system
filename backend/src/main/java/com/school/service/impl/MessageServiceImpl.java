package com.school.service.impl;

import com.school.common.BusinessException;
import com.school.dto.ConversationSummary;
import com.school.dto.MessageResponse;
import com.school.dto.UserBrief;
import com.school.entity.MsgConversation;
import com.school.entity.MsgConversationMember;
import com.school.entity.MsgMessage;
import com.school.entity.SysUser;
import com.school.repository.MsgConversationMemberRepository;
import com.school.repository.MsgConversationRepository;
import com.school.repository.MsgMessageRepository;
import com.school.repository.SysUserRepository;
import com.school.service.MessageService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息服务实现（1:1 单聊）
 */
@Service
public class MessageServiceImpl implements MessageService {

    /** 单条消息最大长度 */
    private static final int MAX_CONTENT_LENGTH = 2000;

    /** 单次最多返回条数 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 联系人候选列表最多返回条数 */
    private static final int CONTACT_LIMIT = 50;

    private final MsgConversationRepository conversationRepository;
    private final MsgConversationMemberRepository memberRepository;
    private final MsgMessageRepository messageRepository;
    private final SysUserRepository userRepository;

    public MessageServiceImpl(MsgConversationRepository conversationRepository,
                              MsgConversationMemberRepository memberRepository,
                              MsgMessageRepository messageRepository,
                              SysUserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.memberRepository = memberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSummary> listConversations(Long userId) {
        List<MsgConversationMember> memberships = memberRepository.findByUserId(userId);
        if (memberships.isEmpty()) {
            return List.of();
        }

        List<Long> conversationIds = memberships.stream()
                .map(MsgConversationMember::getConversationId)
                .toList();

        Map<Long, MsgConversation> conversations = conversationRepository.findByIdIn(conversationIds).stream()
                .collect(Collectors.toMap(MsgConversation::getId, c -> c));

        // 每个会话的最后一条消息
        Map<Long, MsgMessage> lastByConversation = messageRepository.findLastMessages(conversationIds).stream()
                .collect(Collectors.toMap(MsgMessage::getConversationId, m -> m));

        // 每个会话的未读数（一次查询）
        Map<Long, Long> unreadByConversation = new HashMap<>();
        for (Object[] row : messageRepository.countUnreadByUser(userId)) {
            unreadByConversation.put((Long) row[0], ((Number) row[1]).longValue());
        }

        // 单聊对方
        Map<Long, Long> peerIdByConversation = new HashMap<>();
        for (MsgConversationMember member : memberRepository.findByConversationIdIn(conversationIds)) {
            if (!member.getUserId().equals(userId)) {
                peerIdByConversation.put(member.getConversationId(), member.getUserId());
            }
        }

        Set<Long> peerIds = new HashSet<>(peerIdByConversation.values());
        Map<Long, SysUser> users = userRepository.findAllById(peerIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        List<ConversationSummary> result = new ArrayList<>();
        for (Long conversationId : conversationIds) {
            MsgConversation conversation = conversations.get(conversationId);
            MsgMessage last = lastByConversation.get(conversationId);
            // 没有任何消息的会话不展示，避免空壳
            if (conversation == null || last == null) {
                continue;
            }

            ConversationSummary summary = new ConversationSummary();
            summary.setId(conversationId);

            SysUser peer = users.get(peerIdByConversation.get(conversationId));
            if (peer != null) {
                summary.setPeer(new UserBrief(peer.getId(), peer.getUsername(), peer.getRealName()));
            }
            summary.setLastMessage(last.getContent());
            summary.setLastSenderId(last.getSenderId());
            summary.setLastMessageTime(last.getCreatedAt());
            summary.setUnreadCount(unreadByConversation.getOrDefault(conversationId, 0L));
            result.add(summary);
        }

        result.sort((a, b) -> {
            if (a.getLastMessageTime() == null && b.getLastMessageTime() == null) {
                return 0;
            }
            if (a.getLastMessageTime() == null) {
                return 1;
            }
            if (b.getLastMessageTime() == null) {
                return -1;
            }
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });
        return result;
    }

    @Override
    @Transactional
    public ConversationSummary startConversation(Long userId, Long peerUserId) {
        if (peerUserId == null) {
            throw new BusinessException(400, "请选择聊天对象");
        }
        if (peerUserId.equals(userId)) {
            throw new BusinessException(400, "不能给自己发消息");
        }

        SysUser peer = userRepository.findById(peerUserId)
                .orElseThrow(() -> new BusinessException(400, "用户不存在"));
        if (peer.getStatus() == null || peer.getStatus() != 1) {
            throw new BusinessException(400, "该用户当前不可接收消息");
        }

        String pairKey = buildPairKey(userId, peerUserId);
        MsgConversation conversation = conversationRepository.findByPairKey(pairKey).orElse(null);
        if (conversation == null) {
            try {
                conversation = createConversation(userId, peerUserId, pairKey);
            } catch (DataIntegrityViolationException e) {
                // 并发创建：另一个请求已经建好，直接复用
                conversation = conversationRepository.findByPairKey(pairKey)
                        .orElseThrow(() -> new BusinessException(500, "创建会话失败，请重试"));
            }
        }

        return buildSummary(conversation, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(Long userId, Long conversationId, Long beforeId,
                                              Long afterId, int size) {
        requireMember(conversationId, userId);

        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(0, pageSize);

        List<MsgMessage> messages;
        if (afterId != null && afterId > 0) {
            // 轮询增量：比 afterId 更新的消息，正序
            messages = messageRepository.findByConversationIdAndIdGreaterThanOrderByIdAsc(
                    conversationId, afterId, pageable);
        } else {
            if (beforeId != null && beforeId > 0) {
                messages = messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(
                        conversationId, beforeId, pageable);
            } else {
                messages = messageRepository.findByConversationIdOrderByIdDesc(conversationId, pageable);
            }
            // 仓储返回倒序，转成正序便于前端直接渲染
            messages = new ArrayList<>(messages);
            Collections.reverse(messages);
        }

        return toResponses(messages);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, String content) {
        MsgConversationMember member = requireMember(conversationId, userId);

        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) {
            throw new BusinessException(400, "消息内容不能为空");
        }
        if (text.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(400, "消息内容不能超过 " + MAX_CONTENT_LENGTH + " 字");
        }

        MsgMessage message = new MsgMessage();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setContent(text);
        message.setContentType("TEXT");
        message.setCreatedAt(LocalDateTime.now());
        messageRepository.save(message);

        // 发送人视为已读到自己刚发的消息
        member.setLastReadMessageId(message.getId());
        memberRepository.save(member);

        return toResponses(List.of(message)).get(0);
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long conversationId) {
        MsgConversationMember member = requireMember(conversationId, userId);
        Long maxId = messageRepository.findMaxMessageId(conversationId);
        if (maxId != null && (member.getLastReadMessageId() == null || maxId > member.getLastReadMessageId())) {
            member.setLastReadMessageId(maxId);
            memberRepository.save(member);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return messageRepository.countUnreadTotal(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserBrief> contacts(Long userId, String keyword) {
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return userRepository.searchActiveUsers(userId, kw, PageRequest.of(0, CONTACT_LIMIT)).stream()
                .map(u -> new UserBrief(u.getId(), u.getUsername(), u.getRealName()))
                .toList();
    }

    private MsgConversationMember requireMember(Long conversationId, Long userId) {
        return memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new BusinessException(403, "无权访问该会话"));
    }

    private MsgConversation createConversation(Long userId, Long peerUserId, String pairKey) {
        MsgConversation conversation = new MsgConversation();
        conversation.setType("SINGLE");
        conversation.setPairKey(pairKey);
        conversation.setCreatedBy(userId);
        conversation.setCreatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        saveMember(conversation.getId(), userId);
        saveMember(conversation.getId(), peerUserId);
        return conversation;
    }

    private void saveMember(Long conversationId, Long userId) {
        MsgConversationMember member = new MsgConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setJoinedAt(LocalDateTime.now());
        memberRepository.save(member);
    }

    private ConversationSummary buildSummary(MsgConversation conversation, Long userId) {
        ConversationSummary summary = new ConversationSummary();
        summary.setId(conversation.getId());

        for (MsgConversationMember member : memberRepository.findByConversationIdIn(List.of(conversation.getId()))) {
            if (!member.getUserId().equals(userId)) {
                userRepository.findById(member.getUserId()).ifPresent(peer ->
                        summary.setPeer(new UserBrief(peer.getId(), peer.getUsername(), peer.getRealName())));
            }
        }

        messageRepository.findTopByConversationIdOrderByIdDesc(conversation.getId()).ifPresent(last -> {
            summary.setLastMessage(last.getContent());
            summary.setLastSenderId(last.getSenderId());
            summary.setLastMessageTime(last.getCreatedAt());
        });
        summary.setUnreadCount(0);
        return summary;
    }

    private List<MessageResponse> toResponses(List<MsgMessage> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }

        Set<Long> senderIds = messages.stream().map(MsgMessage::getSenderId).collect(Collectors.toSet());
        Map<Long, SysUser> users = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));

        List<MessageResponse> result = new ArrayList<>(messages.size());
        for (MsgMessage message : messages) {
            MessageResponse response = new MessageResponse();
            response.setId(message.getId());
            response.setConversationId(message.getConversationId());
            response.setSenderId(message.getSenderId());
            SysUser sender = users.get(message.getSenderId());
            response.setSenderName(sender == null ? "" : sender.getRealName());
            response.setContent(message.getContent());
            response.setContentType(message.getContentType());
            response.setCreatedAt(message.getCreatedAt());
            result.add(response);
        }
        return result;
    }

    private String buildPairKey(Long userA, Long userB) {
        long min = Math.min(userA, userB);
        long max = Math.max(userA, userB);
        return min + "_" + max;
    }
}
