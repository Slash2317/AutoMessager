package com.slash.automessager;

import com.slash.automessager.domain.AutoMessageBot;

import java.util.HashMap;
import java.util.Map;

public class BotCache {

    private AutoMessageBot bot;
    private Map<Long, String> guildIdToPrefix = new HashMap<>();

    public AutoMessageBot getBot() {
        return bot;
    }

    public void setBot(AutoMessageBot bot) {
        this.bot = bot;
    }

    public Map<Long, String> getGuildIdToPrefix() {
        return guildIdToPrefix;
    }

    public void setGuildIdToPrefix(Map<Long, String> guildIdToPrefix) {
        this.guildIdToPrefix = guildIdToPrefix;
    }
}
