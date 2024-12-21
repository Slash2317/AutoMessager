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
            UPDATE AutoMessageCommand
            SET LastRunDate = DATEADD(MINUTE, DATEDIFF(MINUTE, 0, GETDATE()), 0)
            OUTPUT inserted.GuildDiscordID, inserted.ChannelDiscordID, inserted.Content
            WHERE AutoMessageBotID = :botId
                AND LastRunDate <= DATEADD(MINUTE, Minutes * -1, GETDATE())""")
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
