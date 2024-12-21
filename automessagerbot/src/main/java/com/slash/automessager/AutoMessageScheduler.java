package com.slash.automessager;

import com.slash.automessager.domain.AutoMessageBot;
import com.slash.automessager.domain.PendingMessage;
import com.slash.automessager.repository.AutoMessageCommandRepository;
import com.slash.automessager.service.AutoMessageBotService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AutoMessageScheduler {

    private static final String[] ACTIVITIES = new String[] { "Automating messages", "%s messages sent", ">help" };

    private final JDA jda;
    private final AutoMessageBotService botService;
    private final AutoMessageCommandRepository commandRepository;

    private int activityIndex = -1;

    @Autowired
    public AutoMessageScheduler(JDA jda, AutoMessageBotService botService, AutoMessageCommandRepository commandRepository) {
        this.jda = jda;
        this.botService = botService;
        this.commandRepository = commandRepository;
    }

    @Scheduled(initialDelay = 5000, fixedRate = 60000)
    public void run() {
        int sentMessagesCount = 0;
        try {
            AutoMessageBot bot = botService.getCurrentBot();
            if (bot == null) {
                return;
            }
            sentMessagesCount = bot.getSentMessagesCount();
            List<PendingMessage> pendingMessages = commandRepository.findAndUpdatePendingMessages(bot.getId());
            if (pendingMessages.isEmpty()) {
                return;
            }
            sentMessagesCount += pendingMessages.size();
            botService.updateMessageCount(bot.getId(), sentMessagesCount);

            Map<Long, List<PendingMessage>> guildIdToPendingMessages = pendingMessages.stream().collect(Collectors.groupingBy(PendingMessage::getGuildId));
            for (Map.Entry<Long, List<PendingMessage>> entry : guildIdToPendingMessages.entrySet()) {
                Guild guild = jda.getGuildById(entry.getKey());

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
        finally {
            activityIndex = (activityIndex + 1) % 3;
            String activity = ACTIVITIES[activityIndex];
            if (activityIndex == 1) {
                activity = String.format(activity, sentMessagesCount);
            }
            jda.getPresence().setActivity(Activity.playing(activity));
        }
    }
}
