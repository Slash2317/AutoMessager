package com.slash.automessager.service;

import com.slash.automessager.BotCache;
import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.domain.AutoMessageGuild;
import com.slash.automessager.domain.BasicGuildInfo;

import java.util.List;

public interface AutoMessageBotService {
    void updateMessageCount(Integer sentMessagesCount);
    BasicGuildInfo loadGuildInfo(Long guildDiscordId);
    Integer insertGuild(AutoMessageGuild guild);
    void updateGuildPrefix(Integer guildId, Long guildDiscordId, String prefix);
    List<AutoMessageCommand> getCommandsForGuild(Long guildDiscordId);
    void insertCommand(AutoMessageCommand command);
    void deleteCommands(List<Integer> commandIds);
    BotCache createBotCache();
}
