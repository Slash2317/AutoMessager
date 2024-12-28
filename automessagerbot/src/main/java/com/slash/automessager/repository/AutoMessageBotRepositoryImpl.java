package com.slash.automessager.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoMessageBotRepositoryImpl implements AutoMessageBotSqlRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<Long, String> loadPrefixes(Integer botId) {
        List<Object[]> rows = entityManager.createNativeQuery("SELECT GuildDiscordID, Prefix FROM AutoMessageGuild WHERE AutoMessageBotID = :botId AND Prefix IS NOT NULL")
                .setParameter("botId", botId)
                .getResultList();

        Map<Long, String> guildIdToPrefix = new HashMap<>();
        for (Object[] row : rows) {
            guildIdToPrefix.put(((Number) row[0]).longValue(), row[1].toString());
        }
        return guildIdToPrefix;
    }
}
