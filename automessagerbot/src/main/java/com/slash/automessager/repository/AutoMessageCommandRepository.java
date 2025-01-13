package com.slash.automessager.repository;

import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.domain.PendingMessage;

import java.util.List;

public interface AutoMessageCommandRepository {
    List<PendingMessage> findAndUpdatePendingMessages(Integer botId);
    List<AutoMessageCommand> findAllByBotIdAndGuildDiscordId(Integer botId, Long guildDiscordId);
    void insertCommand(AutoMessageCommand command);
    void deleteAllByIdIn(List<Integer> commandIds);
    int countByBotIdAndGuildDiscordIdAndChannelDiscordId(Integer botId, Long guildDiscordId, Long channelDiscordId);
}
