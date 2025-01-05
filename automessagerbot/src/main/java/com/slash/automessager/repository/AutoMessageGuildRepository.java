package com.slash.automessager.repository;

import com.slash.automessager.domain.AutoMessageGuild;
import com.slash.automessager.domain.BasicGuildInfo;

public interface AutoMessageGuildRepository {
    BasicGuildInfo findGuildInfoByBotIdAndGuildDiscordId(Integer botId, Long guildDiscordId);
    void updateGuildPrefix(Integer guildId, String prefix);
    Integer insertGuild(AutoMessageGuild guild);
}
