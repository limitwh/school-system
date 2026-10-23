package com.school.repository;

import com.school.entity.MsgConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 会话成员数据访问层
 */
@Repository
public interface MsgConversationMemberRepository extends JpaRepository<MsgConversationMember, Long> {

    List<MsgConversationMember> findByUserId(Long userId);

    List<MsgConversationMember> findByConversationIdIn(Collection<Long> conversationIds);

    Optional<MsgConversationMember> findByConversationIdAndUserId(Long conversationId, Long userId);
}
