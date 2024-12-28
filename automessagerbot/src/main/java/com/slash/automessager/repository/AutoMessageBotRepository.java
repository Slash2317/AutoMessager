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
    @Query(value = "UPDATE AutoMessageBot SET SentMessagesCount = ?2 WHERE AutoMessageBotID = ?1", nativeQuery = true)
    void updateMessageCount(Integer botId, Integer sentMessagesCount);
}
