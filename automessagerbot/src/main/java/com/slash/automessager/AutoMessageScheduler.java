package com.slash.automessager;

import com.slash.automessager.domain.AutoMessageBot;
import com.slash.automessager.domain.PendingMessage;
import com.slash.automessager.repository.AutoMessageCommandRepository;
import com.slash.automessager.repository.AutoMessageCommandRepositoryImpl;
import com.slash.automessager.service.AutoMessageBotService;
import com.slash.automessager.service.AutoMessageBotServiceImpl;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoMessageScheduler {

    private static final String[] ACTIVITIES = new String[] { "Automating messages", "%s messages sent", ">help" };

    private final AutoMessageBotService botService;
    private final AutoMessageCommandRepository commandRepository;

    private int activityIndex = -1;

    public AutoMessageScheduler() {
        this(new AutoMessageBotServiceImpl(), new AutoMessageCommandRepositoryImpl());
    }

    public AutoMessageScheduler(AutoMessageBotService botService, AutoMessageCommandRepository commandRepository) {
        this.botService = botService;
        this.commandRepository = commandRepository;
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::run, 5, 60, TimeUnit.SECONDS);
    }

    private void run() {
        int sentMessagesCount = 0;
        try {
            AutoMessageBot bot = Application.getBotCache().getBot();
            if (bot == null) {
                return;
            }
            sentMessagesCount = bot.getSentMessagesCount();
            List<PendingMessage> pendingMessages = commandRepository.findAndUpdatePendingMessages(bot.getId());
            if (pendingMessages.isEmpty()) {
                return;
            }
            sentMessagesCount += pendingMessages.size();
            botService.updateMessageCount(sentMessagesCount);

            Map<Long, List<PendingMessage>> guildIdToPendingMessages = pendingMessages.stream().collect(Collectors.groupingBy(PendingMessage::getGuildId));
            for (Map.Entry<Long, List<PendingMessage>> entry : guildIdToPendingMessages.entrySet()) {
                Guild guild = Application.getJda().getGuildById(entry.getKey());
                if (guild == null) {
                    continue;
                }
                for (PendingMessage pendingMessage : entry.getValue()) {
                    TextChannel channel = guild.getTextChannelById(pendingMessage.getChannelId());
                    if (channel == null) {
                        continue;
                    }
                    channel.sendMessage(pendingMessage.getContent()).queue();
                }
            }
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        finally {
            activityIndex = (activityIndex + 1) % 3;
            String activity = ACTIVITIES[activityIndex];
            if (activityIndex == 1) {
                activity = String.format(activity, sentMessagesCount);
            }
            Application.getJda().getPresence().setActivity(Activity.playing(activity));
        }
    }
}
