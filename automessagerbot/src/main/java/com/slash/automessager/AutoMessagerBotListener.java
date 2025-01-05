package com.slash.automessager;

import com.slash.automessager.handler.AutoMessageRequestHandler;
import com.slash.automessager.handler.MiscRequestHandler;
import com.slash.automessager.request.*;
import com.slash.automessager.service.AutoMessageBotService;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AutoMessagerBotListener extends ListenerAdapter {

    private final AutoMessageRequestHandler autoMessageRequestHandler;
    private final MiscRequestHandler miscRequestHandler;
    private final AutoMessageBotService botService;

    @Autowired
    public AutoMessagerBotListener(AutoMessageRequestHandler autoMessageRequestHandler, MiscRequestHandler miscRequestHandler, AutoMessageBotService botService) {
        this.autoMessageRequestHandler = autoMessageRequestHandler;
        this.miscRequestHandler = miscRequestHandler;
        this.botService = botService;
    }

    @Override
    @Transactional
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        Long guildId = event.getGuild().getIdLong();
        String prefix = botService.getPrefixCache().get(guildId);
        if (prefix == null) {
            prefix = ">";
        }
        if (!event.getMessage().getContentRaw().startsWith(prefix)) {
            return;
        }

        handleEvent(new MessageRequestContext(event, prefix, botService.getCurrentBot()));
    }

    private void handleEvent(RequestContext requestContext) {
        if (requestContext.getCommand() == null) {
            return;
        }

        switch (requestContext.getCommand()) {
            case VIEW -> autoMessageRequestHandler.handleViewCommand(requestContext);
            case SETUP -> autoMessageRequestHandler.handleSetupCommand(requestContext);
            case REMOVE -> autoMessageRequestHandler.handleRemoveCommand(requestContext);
            case HELP -> miscRequestHandler.handleHelpCommand(requestContext);
            case PREFIX -> miscRequestHandler.handlePrefixCommand(requestContext);
            case VOTE -> miscRequestHandler.handleVoteCommand(requestContext);
        }
    }

    @Override
    @Transactional
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        handleEvent(new SlashRequestContext(event, botService.getCurrentBot()));
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        for (Command command : Command.values()) {
            SlashCommandData slashCommandData = Commands.slash(command.getCommandName(), command.getDescription());
            for (OptionInfo optionInfo : command.getParameters()) {
                slashCommandData.addOption(optionInfo.optionType(), optionInfo.name(), optionInfo.description(), true);
            }

            commandData.add(slashCommandData);
        }
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
