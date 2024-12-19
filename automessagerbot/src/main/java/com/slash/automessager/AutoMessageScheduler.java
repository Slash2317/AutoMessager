package com.slash.automessager;

import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.domain.Data;
import com.slash.automessager.repository.DataRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AutoMessageScheduler {

    private static final String[] ACTIVITIES = new String[] { "Automating messages", "%s messages sent", ">help" };

    private final JDA jda;
    private final DataRepository dataRepository;

    private int activityIndex = -1;

    @Autowired
    public AutoMessageScheduler(JDA jda, DataRepository dataRepository) {
        this.jda = jda;
        this.dataRepository = dataRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void run() {
        Data data = dataRepository.loadData();
        try {
            if (data == null || data.getGuildIdToAutoMessageCommands().isEmpty()) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            Map<String, List<AutoMessageCommand>> guildIdToPendingCommands = new HashMap<>();
            for (Map.Entry<String, List<AutoMessageCommand>> entry : data.getGuildIdToAutoMessageCommands().entrySet()) {
                List<AutoMessageCommand> pendingCommands = entry.getValue()
                        .stream()
                        .filter(c -> Duration.between(c.getDateRan(), now).toMinutes() >= c.getMinutes())
                        .toList();

                if (pendingCommands.isEmpty()) {
                    continue;
                }

                pendingCommands.forEach(c -> c.setDateRan(now));
                guildIdToPendingCommands.put(entry.getKey(), pendingCommands);
            }

            if (guildIdToPendingCommands.isEmpty()) {
                return;
            }

            int messagesSent = 0;
            for (Map.Entry<String, List<AutoMessageCommand>> entry : guildIdToPendingCommands.entrySet()) {
                String guildId = data.getGuildIdToAutoMessageCommands().keySet().iterator().next();
                Guild guild = jda.getGuildById(guildId);

                if (guild == null) {
                    return;
                }

                for (AutoMessageCommand command : entry.getValue()) {
                    TextChannel channel = guild.getTextChannelById(command.getChannelId());
                    if (channel == null) {
                        continue;
                    }
                    channel.sendMessage(command.getMessage()).queue();
                    messagesSent++;
                }
            }
            data.setMessagesSent(data.getMessagesSent() + messagesSent);
            dataRepository.saveData(data);
        }
        finally {
            activityIndex = (activityIndex + 1) % 3;
            String activity = ACTIVITIES[activityIndex];
            if (activityIndex == 1) {
                int messagesSent = data != null ? data.getMessagesSent() : 0;
                activity = String.format(activity, messagesSent);
            }
            jda.getPresence().setActivity(Activity.playing(activity));
        }
    }
}
