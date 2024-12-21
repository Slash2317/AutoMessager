package com.slash.automessager.service;

import com.slash.automessager.domain.AutoMessageBot;

public interface AutoMessageBotService {
    AutoMessageBot getCurrentBot();
    void updateMessageCount(Integer botId, Integer sentMessagesCount);
    void save(AutoMessageBot bot);
}
