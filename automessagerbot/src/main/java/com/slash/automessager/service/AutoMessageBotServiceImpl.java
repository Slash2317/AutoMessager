package com.slash.automessager.service;

import com.slash.automessager.Application;
import com.slash.automessager.BotCache;
import com.slash.automessager.domain.AutoMessageBot;
import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.domain.AutoMessageGuild;
import com.slash.automessager.domain.BasicGuildInfo;
import com.slash.automessager.repository.*;

import java.util.List;

public class AutoMessageBotServiceImpl implements AutoMessageBotService {

    private final AutoMessageBotRepository botRepository;
    private final AutoMessageGuildRepository guildRepository;
    private final AutoMessageCommandRepository commandRepository;

    public AutoMessageBotServiceImpl() {
        this(new AutoMessageBotRepositoryImpl(), new AutoMessageGuildRepositoryImpl(), new AutoMessageCommandRepositoryImpl());
    }

    public AutoMessageBotServiceImpl(AutoMessageBotRepository botRepository, AutoMessageGuildRepository guildRepository, AutoMessageCommandRepository commandRepository) {
        this.botRepository = botRepository;
        this.guildRepository = guildRepository;
        this.commandRepository = commandRepository;
    }

    @Override
    public void updateMessageCount(Integer sentMessagesCount) {
        botRepository.updateMessageCount(Application.getBotCache().getBot().getId(), sentMessagesCount);
        Application.getBotCache().getBot().setSentMessagesCount(sentMessagesCount);
    }

    @Override
    public BasicGuildInfo loadGuildInfo(Long guildDiscordId) {
        return guildRepository.findGuildInfoByBotIdAndGuildDiscordId(Application.getBotCache().getBot().getId(), guildDiscordId);
    }

    @Override
    public Integer insertGuild(AutoMessageGuild guild) {
        Integer guildId = guildRepository.insertGuild(guild);
        if (guild.getPrefix() != null) {
            Application.getBotCache().getGuildIdToPrefix().put(guild.getDiscordId(), guild.getPrefix());
        }
        return guildId;
    }

    @Override
    public void updateGuildPrefix(Integer guildId, Long guildDiscordId, String prefix) {
        guildRepository.updateGuildPrefix(guildId, prefix);
        Application.getBotCache().getGuildIdToPrefix().put(guildDiscordId, prefix);
    }

    @Override
    public int getNumOfCommands(Long guildDiscordId, Long channelDiscordId) {
        return commandRepository.countByBotIdAndGuildDiscordIdAndChannelDiscordId(Application.getBotCache().getBot().getId(), guildDiscordId, channelDiscordId);
    }

    @Override
    public List<AutoMessageCommand> getCommandsForGuild(Long guildDiscordId) {
        return commandRepository.findAllByBotIdAndGuildDiscordId(Application.getBotCache().getBot().getId(), guildDiscordId);
    }

    @Override
    public void insertCommand(AutoMessageCommand command) {
        commandRepository.insertCommand(command);
    }

    @Override
    public void deleteCommands(List<Integer> commandIds) {
        commandRepository.deleteAllByIdIn(commandIds);
    }

    @Override
    public BotCache createBotCache() {
        BotCache botCache = new BotCache();
        Long discordId = Application.getJda().getSelfUser().getApplicationIdLong();
        AutoMessageBot bot = botRepository.findByDiscordId(discordId).orElse(null);
        if (bot == null) {
            bot = new AutoMessageBot();
            bot.setDiscordId(Application.getJda().getSelfUser().getApplicationIdLong());
            bot.setSentMessagesCount(0);
            bot.setId(botRepository.insertBot(bot));
        }
        else {
            botCache.setGuildIdToPrefix(botRepository.loadPrefixes(bot.getId()));
        }
        botCache.setBot(bot);
        return botCache;
    }
}
