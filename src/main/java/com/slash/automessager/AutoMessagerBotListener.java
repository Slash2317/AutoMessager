package com.slash.automessager;

import com.slash.automessager.handler.AutoMessageRequestHandler;
import com.slash.automessager.handler.MiscRequestHandler;
import com.slash.automessager.request.RequestContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AutoMessagerBotListener extends ListenerAdapter {

    private final AutoMessageRequestHandler autoMessageRequestHandler;
    private final MiscRequestHandler miscRequestHandler;

    @Autowired
    public AutoMessagerBotListener(AutoMessageRequestHandler autoMessageRequestHandler, MiscRequestHandler miscRequestHandler) {
        this.autoMessageRequestHandler = autoMessageRequestHandler;
        this.miscRequestHandler = miscRequestHandler;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        RequestContext requestContext = RequestContext.from(event);

        if (requestContext.command() == null) {
            return;
        }

        switch (requestContext.command()) {
            case VIEW -> autoMessageRequestHandler.handleViewCommand(requestContext);
            case SETUP -> autoMessageRequestHandler.handleSetupCommand(requestContext);
            case REMOVE -> autoMessageRequestHandler.handleRemoveCommand(requestContext);
            case HELP -> miscRequestHandler.handleHelpCommand(requestContext);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.equals("help")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.decode("#a020f0"))
                    .setDescription("""
                        **Welcome!** :smiley:
                        Currently, this bot, Tiziland Bot, does **not** support slash commands for coding and modifying to be easier.
                        Instead, please use the prefix `t!` in all your commands. For example, t!help.
                        **For the actual commands list, please use the t!help command in any chat this bot is enabled on.**""");

            event.replyEmbeds(embedBuilder.build()).setAllowedMentions(Collections.emptyList()).queue();
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("help", "Gives info about how to use this bot"));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
