package com.slash.automessager.handler;

import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.request.Command;
import com.slash.automessager.request.RequestContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MiscRequestHandlerImpl implements MiscRequestHandler {

    private static final Color DISCORD_BLUE = Color.decode("#5566f2");

    @Override
    public void handleHelpCommand(RequestContext requestContext) {
        String commandsDisplay = Arrays.stream(Command.values()).map(Command::getFullDescription).collect(Collectors.joining("\n"));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":blue_circle: AUTO-MESSAGER | COMMANDS")
                .setColor(DISCORD_BLUE)
                .setDescription(commandsDisplay)
                .build();

        requestContext.event().getChannel().sendMessageEmbeds(embed).setAllowedMentions(Collections.emptyList()).queue();
    }
}
