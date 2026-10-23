package com.school.repository;

import com.school.entity.MsgConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 消息会话数据访问层
 */
@Repository
public interface MsgConversationRepository extends JpaRepository<MsgConversation, Long> {

    /** 按单聊去重键查找会话 */
    Optional<MsgConversation> findByPairKey(String pairKey);

    List<MsgConversation> findByIdIn(Collection<Long> ids);
}
