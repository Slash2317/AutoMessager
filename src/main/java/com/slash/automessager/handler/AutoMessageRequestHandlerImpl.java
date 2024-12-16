package com.slash.automessager.handler;

import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.exception.InvalidPermissionException;
import com.slash.automessager.repository.DataRepository;
import com.slash.automessager.request.RequestContext;
import com.slash.automessager.domain.Data;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

@Service
public class AutoMessageRequestHandlerImpl implements AutoMessageRequestHandler {

    private final DataRepository dataRepository;

    @Autowired
    public AutoMessageRequestHandlerImpl(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public void handleSetupCommand(RequestContext requestContext) {
        try {
            if (requestContext.event().getMember() == null || !canManageChannels(requestContext)) {
                throw new InvalidPermissionException("You do not have permission to add auto message commands");
            }
            if (requestContext.arguments() == null) {
                throw new IllegalArgumentException("No arguments supplied");
            }

            List<String> arguments = getArguments(requestContext.arguments(), " ", 3);
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Expected 3 arguments");
            }
            Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(arguments.get(0));
            if (!matcher.find()) {
                throw new IllegalArgumentException("First argument should be a channel");
            }

            String channelMention = matcher.group();
            String channelId = channelMention.substring(2, channelMention.length() - 1);
            Integer minutes = Integer.valueOf(arguments.get(1));
            String message = arguments.get(2);

            String guildId = requestContext.event().getGuild().getId();
            Data data = dataRepository.loadData();
            if (data == null) {
                data = new Data();
            }

            data.getGuildIdToAutoMessageCommands().putIfAbsent(guildId, new ArrayList<>());
            List<AutoMessageCommand> commands = data.getGuildIdToAutoMessageCommands().get(guildId);

            GuildChannel guildChannel = requestContext.event().getGuild().getGuildChannelById(channelId);
            if (guildChannel != null) {
                AutoMessageCommand command = new AutoMessageCommand();
                command.setChannelId(guildChannel.getId());
                command.setMinutes(minutes);
                command.setMessage(message);
                command.setDateRan(LocalDateTime.now());
                commands.add(command);
            }

            dataRepository.saveData(data);

            String channelNames = data.getAutoMessageCommandsDisplay(requestContext.event().getGuild());
            requestContext.event().getChannel().sendMessage("Channel(s) added. The current auto message commands are:\n" + channelNames).queue();
        }
        catch (InvalidPermissionException e) {
            requestContext.event().getChannel().sendMessage(e.getMessage()).queue();
        }
        catch (IllegalArgumentException e) {
            requestContext.event().getChannel().sendMessage("The command must follow this format `" + requestContext.command().getFullDescription() + "`").queue();
        }
    }

    @Override
    public void handleRemoveCommand(RequestContext requestContext) {
        try {
            if (requestContext.event().getMember() == null || !canManageChannels(requestContext)) {
                throw new InvalidPermissionException("You do not have permission to remove auto message commands");
            }
            if (requestContext.arguments() == null) {
                throw new IllegalArgumentException("No arguments supplied");
            }

            List<String> arguments = getArguments(requestContext.arguments(), " ", 1);
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Expected 1 argument");
            }

            Data data = dataRepository.loadData();
            List<AutoMessageCommand> commands = data != null ? data.getGuildIdToAutoMessageCommands().get(requestContext.event().getGuild().getId()) : new ArrayList<>();
            if (commands.isEmpty()) {
                requestContext.event().getChannel().sendMessage("There are currently no auto message commands").queue();
                return;
            }

            try {
                int commandIndex = Integer.parseInt(arguments.get(0)) - 1;
                if (commandIndex < 0 || commandIndex >= commands.size()) {
                    throw new IllegalArgumentException("Invalid index");
                }
                commands.remove(commandIndex);
                dataRepository.saveData(data);

                if (commands.isEmpty()) {
                    requestContext.event().getChannel().sendMessage("Command removed. There are currently no auto message commands").queue();
                }
                else {
                    String commandsDisplay = data.getAutoMessageCommandsDisplay(requestContext.event().getGuild());
                    requestContext.event().getChannel().sendMessage("Command removed. The current auto message commands are:\n\n" + commandsDisplay).queue();
                }
                return;
            }
            catch (NumberFormatException ignore) {

            }

            Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(arguments.get(0));
            if (!matcher.find()) {
                throw new IllegalArgumentException("First argument should be a channel or number");
            }

            String channelMention = matcher.group();
            String channelId = channelMention.substring(2, channelMention.length() - 1);
            List<AutoMessageCommand> commandsToRemove = new ArrayList<>();
            for (AutoMessageCommand command : commands) {
                if (command.getChannelId().equals(channelId)) {
                    commandsToRemove.add(command);
                }
            }

            commands.removeAll(commandsToRemove);
            dataRepository.saveData(data);

            if (commands.isEmpty()) {
                requestContext.event().getChannel().sendMessage("Command removed. There are currently no auto message commands").queue();
            }
            else {
                String commandsDisplay = data.getAutoMessageCommandsDisplay(requestContext.event().getGuild());
                requestContext.event().getChannel().sendMessage("Command removed. The current auto message commands are:\n\n" + commandsDisplay).queue();
            }
        }
        catch (InvalidPermissionException e) {
            requestContext.event().getChannel().sendMessage(e.getMessage()).queue();
        }
        catch (IllegalArgumentException e) {
            requestContext.event().getChannel().sendMessage("The command must follow this format `" + requestContext.command().getFullDescription() + "`").queue();
        }
    }

    @Override
    public void handleViewCommand(RequestContext requestContext) {
        try {
            if (requestContext.event().getMember() == null || !canManageChannels(requestContext)) {
                throw new InvalidPermissionException("You do not have permission to view auto message commands");
            }
            String guildId = requestContext.event().getGuild().getId();
            Data data = dataRepository.loadData();
            if (data == null) {
                data = new Data();
            }

            if (data.getGuildIdToAutoMessageCommands().containsKey(guildId)) {
                List<AutoMessageCommand> autoMessageCommands = data.getGuildIdToAutoMessageCommands().get(guildId);
                if (autoMessageCommands.isEmpty()) {
                    requestContext.event().getChannel().sendMessage("There are currently no auto message commands.").queue();
                }
                else {
                    String commandsDisplay = data.getAutoMessageCommandsDisplay(requestContext.event().getGuild());
                    requestContext.event().getChannel().sendMessage("The current auto message commands are:\n\n" + commandsDisplay).queue();
                }
            }
            else {
                requestContext.event().getChannel().sendMessage("There are currently no auto message commands.").queue();
            }
        }
        catch (InvalidPermissionException e) {
            requestContext.event().getChannel().sendMessage(e.getMessage()).queue();
        }
        catch (IllegalArgumentException e) {
            requestContext.event().getChannel().sendMessage("The command must follow this format `" + requestContext.command().getFullDescription() + "`").queue();
        }
    }

    private List<String> getArguments(String argumentsString, String delimiter, int numOfArguments) {
        if (argumentsString == null || argumentsString.isEmpty()) {
            return Collections.emptyList();
        }

        if (numOfArguments == 1) {
            return List.of(argumentsString);
        }
        List<String> splitArguments = List.of(argumentsString.split(delimiter));

        if (splitArguments.size() <= numOfArguments) {
            return splitArguments;
        }

        List<String> arguments = new ArrayList<>(splitArguments.subList(0, numOfArguments - 1));
        arguments.add(String.join(delimiter, splitArguments.subList(numOfArguments - 1, splitArguments.size())));
        return arguments;
    }

    private boolean canManageChannels(RequestContext requestContext) {
        Member member = requestContext.event().getMember();
        if (member == null) {
            return false;
        }
        if (member.isOwner()) {
            return true;
        }
        List<Role> roles = member.getRoles();
        for (Role role : roles) {
            if (role.hasPermission(Permission.MANAGE_CHANNEL) || role.hasPermission(Permission.ADMINISTRATOR)) {
                return true;
            }
        }
        return false;
    }
}
