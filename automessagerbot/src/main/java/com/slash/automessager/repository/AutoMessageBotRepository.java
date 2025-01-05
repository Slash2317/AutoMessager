package com.slash.automessager.repository;

import com.slash.automessager.domain.AutoMessageBot;

import java.util.Map;
import java.util.Optional;

public interface AutoMessageBotRepository {
    Optional<AutoMessageBot> findByDiscordId(Long discordId);
    void updateMessageCount(Integer botId, Integer sentMessagesCount);
    Map<Long, String> loadPrefixes(Integer botId);
    Integer insertBot(AutoMessageBot bot);
}
