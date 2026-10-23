package com.school.repository;

import com.school.entity.MsgMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 消息数据访问层
 */
@Repository
public interface MsgMessageRepository extends JpaRepository<MsgMessage, Long> {

    /** 最新一页（倒序） */
    List<MsgMessage> findByConversationIdOrderByIdDesc(Long conversationId, Pageable pageable);

    /** 历史一页：比 beforeId 更早（倒序） */
    List<MsgMessage> findByConversationIdAndIdLessThanOrderByIdDesc(Long conversationId, Long beforeId,
                                                                    Pageable pageable);

    /** 增量：比 afterId 更新的消息（正序） */
    List<MsgMessage> findByConversationIdAndIdGreaterThanOrderByIdAsc(Long conversationId, Long afterId,
                                                                      Pageable pageable);

    Optional<MsgMessage> findTopByConversationIdOrderByIdDesc(Long conversationId);

    /** 批量取每个会话的最后一条消息 */
    @Query("select m from MsgMessage m where m.id in (" +
            "select max(m2.id) from MsgMessage m2 where m2.conversationId in :conversationIds " +
            "group by m2.conversationId)")
    List<MsgMessage> findLastMessages(@Param("conversationIds") Collection<Long> conversationIds);

    /** 按会话统计当前用户的未读数（一次查完所有会话） */
    @Query("select m.conversationId, count(m) from MsgMessage m, MsgConversationMember cm " +
            "where cm.userId = :userId and m.conversationId = cm.conversationId " +
            "and (cm.lastReadMessageId is null or m.id > cm.lastReadMessageId) " +
            "and m.senderId <> :userId " +
            "group by m.conversationId")
    List<Object[]> countUnreadByUser(@Param("userId") Long userId);

    /** 当前用户的未读总数 */
    @Query("select count(m) from MsgMessage m, MsgConversationMember cm " +
            "where cm.userId = :userId and m.conversationId = cm.conversationId " +
            "and (cm.lastReadMessageId is null or m.id > cm.lastReadMessageId) " +
            "and m.senderId <> :userId")
    long countUnreadTotal(@Param("userId") Long userId);

    /** 会话内最大消息 ID（标记已读用） */
    @Query("select max(m.id) from MsgMessage m where m.conversationId = :conversationId")
    Long findMaxMessageId(@Param("conversationId") Long conversationId);
}
