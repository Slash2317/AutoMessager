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
    public void handleHelpCommand(RequestContext requestContext) {
        String commandsDisplay = Arrays.stream(Command.values()).map(c -> c.getFullDescription(requestContext.getPrefix())).collect(Collectors.joining("\n"));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":blue_circle: AUTO-MESSAGER | COMMANDS")
                .setColor(DISCORD_BLUE)
                .setDescription(commandsDisplay)
                .build();

        requestContext.sendMessageEmbeds(embed);
    }

    @Override
    public void handlePrefixCommand(RequestContext requestContext) {
        String prefix = requestContext.getArgument("prefix", String.class);
        if (prefix.isBlank()) {
            return;
        }
        String guildId = requestContext.getGuild().getId();

        Data data = dataRepository.loadData();
        data.getGuildIdToPrefix().put(guildId, prefix);
        dataRepository.saveData(data);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":white_check_mark: Successfully updated prefix to " + prefix)
                .setColor(DISCORD_BLUE)
                .build();

        requestContext.sendMessageEmbeds(embed);
    }

    @Override
    public void handleVoteCommand(RequestContext requestContext) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":envelope_with_arrow: Vote for Auto Messager ")
                .setDescription("""
                        Vote for the bot at Top.gg!
                        https://top.gg/bot/1318005521986486332""")
                .setColor(DISCORD_BLUE)
                .build();

        requestContext.sendMessageEmbeds(embed);
    }
}
