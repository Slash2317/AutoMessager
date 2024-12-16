package com.slash.automessager.handler;

import com.slash.automessager.domain.Data;
import com.slash.automessager.repository.DataRepository;
import com.slash.automessager.request.Command;
import com.slash.automessager.request.RequestContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class MiscRequestHandlerImpl implements MiscRequestHandler {

    private static final Color DISCORD_BLUE = Color.decode("#5566f2");

    private final DataRepository dataRepository;

    @Autowired
    public MiscRequestHandlerImpl(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public void handleHelpCommand(RequestContext requestContext, String prefix) {
        String commandsDisplay = Arrays.stream(Command.values()).map(c -> c.getFullDescription(prefix)).collect(Collectors.joining("\n"));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":blue_circle: AUTO-MESSAGER | COMMANDS")
                .setColor(DISCORD_BLUE)
                .setDescription(commandsDisplay)
                .build();

        requestContext.event().getChannel().sendMessageEmbeds(embed).setAllowedMentions(Collections.emptyList()).queue();
    }

    @Override
    public void handlePrefixCommand(RequestContext requestContext) {
        if (requestContext.arguments().isBlank()) {
            return;
        }
        String guildId = requestContext.event().getGuild().getId();

        Data data = dataRepository.loadData();
        data.getGuildIdToPrefix().put(guildId, requestContext.arguments());
        dataRepository.saveData(data);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":white_check_mark: Successfully updated prefix to " + requestContext.arguments())
                .setColor(DISCORD_BLUE)
                .build();

        requestContext.event().getChannel().sendMessageEmbeds(embed).setAllowedMentions(Collections.emptyList()).queue();
    }
}
