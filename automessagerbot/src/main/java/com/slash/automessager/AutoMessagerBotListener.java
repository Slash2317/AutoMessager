package com.slash.automessager;

import com.slash.automessager.handler.AutoMessageRequestHandler;
import com.slash.automessager.handler.AutoMessageRequestHandlerImpl;
import com.slash.automessager.handler.MiscRequestHandler;
import com.slash.automessager.handler.MiscRequestHandlerImpl;
import com.slash.automessager.request.*;
import com.slash.automessager.service.AutoMessageBotService;
import com.slash.automessager.service.AutoMessageBotServiceImpl;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

public class AutoMessagerBotListener extends ListenerAdapter {

    private final AutoMessageRequestHandler autoMessageRequestHandler;
    private final MiscRequestHandler miscRequestHandler;

    public AutoMessagerBotListener() {
        this(new AutoMessageRequestHandlerImpl(), new MiscRequestHandlerImpl());
    }

    public AutoMessagerBotListener(AutoMessageRequestHandler autoMessageRequestHandler, MiscRequestHandler miscRequestHandler) {
        this.autoMessageRequestHandler = autoMessageRequestHandler;
        this.miscRequestHandler = miscRequestHandler;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        Long guildId = event.getGuild().getIdLong();
        String prefix = Application.getBotCache().getGuildIdToPrefix().get(guildId);
        if (prefix == null) {
            prefix = ">";
        }
        if (!event.getMessage().getContentRaw().startsWith(prefix)) {
            return;
        }

        handleEvent(new MessageRequestContext(event, prefix));
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        handleEvent(new SlashRequestContext(event));
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
