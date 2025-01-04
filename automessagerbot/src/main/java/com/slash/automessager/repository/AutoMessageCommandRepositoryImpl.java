package com.slash.automessager.repository;

import com.slash.automessager.domain.PendingMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AutoMessageCommandRepositoryImpl implements AutoMessageCommandRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<PendingMessage> findAndUpdatePendingMessages(Integer botId) {
        List<Object[]> rows = entityManager.createNativeQuery("""
            UPDATE auto_message_command
            SET last_run_date = DATE_TRUNC('MINUTE', CURRENT_TIMESTAMP)
            WHERE auto_message_bot_id = :botId
                AND last_run_date <= (CURRENT_TIMESTAMP - minutes * INTERVAL '1 minute')
            RETURNING guild_discord_id, channel_discord_id, content""")
                .setParameter("botId", botId)
                .getResultList();

        List<PendingMessage> pendingMessages = new ArrayList<>();
        for (Object[] row : rows) {
            PendingMessage pendingMessage = new PendingMessage();
            pendingMessage.setGuildId(((Number) row[0]).longValue());
            pendingMessage.setChannelId(((Number) row[1]).longValue());
            pendingMessage.setContent((String) row[2]);
            pendingMessages.add(pendingMessage);
        }
        return pendingMessages;
    }
}
