package com.slash.automessager.service;

import com.slash.automessager.domain.AutoMessageBot;

import java.util.Map;

public interface AutoMessageBotService {
    AutoMessageBot getCurrentBot();
    Map<Long, String> getPrefixCache();
    void updatePrefixCache(Long guildId, String prefix);
    void updateMessageCount(Integer botId, Integer sentMessagesCount);
    void save(AutoMessageBot bot);
}
