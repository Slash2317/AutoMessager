package com.slash.automessager.repository;

import com.slash.automessager.domain.AutoMessageBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AutoMessageBotRepository extends JpaRepository<AutoMessageBot, Integer>, AutoMessageBotSqlRepository {
    Optional<AutoMessageBot> findByDiscordId(Long discordId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE auto_message_bot SET sent_messages_count = ?2 WHERE auto_message_bot_id = ?1", nativeQuery = true)
    void updateMessageCount(Integer botId, Integer sentMessagesCount);
}
