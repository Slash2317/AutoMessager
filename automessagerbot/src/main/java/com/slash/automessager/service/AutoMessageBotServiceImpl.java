package com.slash.automessager.service;

import com.slash.automessager.domain.AutoMessageBot;
import com.slash.automessager.repository.AutoMessageBotRepository;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AutoMessageBotServiceImpl implements AutoMessageBotService {

    private final JDA jda;
    private final AutoMessageBotRepository botRepository;
    private final Map<Long, String> guildIdToPrefix = new HashMap<>();

    @Autowired
    public AutoMessageBotServiceImpl(JDA jda, AutoMessageBotRepository botRepository) {
        this.jda = jda;
        this.botRepository = botRepository;
    }

    @Override
    public AutoMessageBot getCurrentBot() {
        Long discordId = jda.getSelfUser().getApplicationIdLong();
        return botRepository.findByDiscordId(discordId).orElse(null);
    }

    @Override
    public Map<Long, String> getPrefixCache() {
        return guildIdToPrefix;
    }

    @Override
    public void updatePrefixCache(Long guildId, String prefix) {
        guildIdToPrefix.put(guildId, prefix);
    }

    @Override
    public void updateMessageCount(Integer botId, Integer sentMessagesCount) {
        botRepository.updateMessageCount(botId, sentMessagesCount);
    }

    @Override
    public void save(AutoMessageBot bot) {
        botRepository.save(bot);
    }

    @EventListener(ApplicationReadyEvent.class)
    private void initialSetup() {
        AutoMessageBot bot = getCurrentBot();
        if (bot == null) {
            bot = new AutoMessageBot();
            bot.setDiscordId(jda.getSelfUser().getApplicationIdLong());
            bot.setSentMessagesCount(0);
            botRepository.save(bot);
        }
        else {
            guildIdToPrefix.putAll(botRepository.loadPrefixes(bot.getId()));
        }
    }
}
