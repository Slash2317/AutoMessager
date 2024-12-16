package com.slash.automessager.handler;

import com.slash.automessager.domain.AutoMessageCommand;
import com.slash.automessager.exception.InvalidPermissionException;
import com.slash.automessager.repository.DataRepository;
import com.slash.automessager.request.RequestContext;
import com.slash.automessager.domain.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Service
public class AutoMessageRequestHandlerImpl implements AutoMessageRequestHandler {

    private static final Color DISCORD_BLUE = Color.decode("#5566f2");

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
            String time = arguments.get(1);
            int minutes;
            if (time.endsWith("h")) {
                minutes = Integer.parseInt(arguments.get(1).substring(0, arguments.get(1).length() - 1)) * 60;
            }
            else if (time.endsWith("m")) {
                minutes = Integer.parseInt(arguments.get(1).substring(0, arguments.get(1).length() - 1));
            }
            else {
                minutes = Integer.parseInt(arguments.get(1));
            }
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
                dataRepository.saveData(data);
                sendSetupEmbed(command, guildChannel, requestContext);
            }
        }
        catch (InvalidPermissionException e) {
            requestContext.event().getChannel().sendMessage(e.getMessage()).queue();
        }
        catch (IllegalArgumentException e) {
            requestContext.event().getChannel().sendMessage("The command must follow this format `" + requestContext.command().getFullDescription(requestContext.prefix()) + "`").queue();
        }
    }

    private void sendSetupEmbed(AutoMessageCommand command, GuildChannel guildChannel, RequestContext requestContext) {
        boolean hours = command.getMinutes() % 60 == 0;
        Integer time = hours ? command.getMinutes() / 60 : command.getMinutes();
        String timeDisplay = hours ? "hour(s)" : "minutes";
        String description = String.format("""
                #%s [%s]
                Every %s %s
                **"%s"**
                
                To see all added channels run >view.""", guildChannel.getName(), command.getChannelId(), time, timeDisplay, command.getMessage());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":white_check_mark: Successfully added new channel")
                .setColor(DISCORD_BLUE)
                .setDescription(description)
                .build();

        requestContext.event().getChannel().sendMessageEmbeds(embed).setAllowedMentions(Collections.emptyList()).queue();
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

            if (!commandsToRemove.isEmpty()) {
                commands.removeAll(commandsToRemove);
                if (commands.isEmpty()) {
                    data.getGuildIdToAutoMessageCommands().remove(requestContext.event().getGuild().getId());
                }
                dataRepository.saveData(data);

                sendRemoveEmbed(commandsToRemove, requestContext.event().getGuild().getGuildChannelById(commandsToRemove.getFirst().getChannelId()), requestContext);
            }
            else {
                requestContext.event().getChannel().sendMessage("There are currently no auto message commands").queue();
            }
        }
        catch (InvalidPermissionException e) {
            requestContext.event().getChannel().sendMessage(e.getMessage()).queue();
        }
        catch (IllegalArgumentException e) {
            requestContext.event().getChannel().sendMessage("The command must follow this format `" + requestContext.command().getFullDescription(requestContext.prefix()) + "`").queue();
        }
    }

    private void sendRemoveEmbed(List<AutoMessageCommand> commands, GuildChannel guildChannel, RequestContext requestContext) {
        String commandDisplays = getCommandDisplays(commands);
        String description = String.format("""
                #%s [%s]
                %s
                
                To see all added channels run >view.""", guildChannel.getName(), guildChannel.getId(), commandDisplays);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":white_check_mark: Successfully removed channel")
                .setColor(DISCORD_BLUE)
                .setDescription(description)
                .build();

        requestContext.event().getChannel().sendMessageEmbeds(embed).setAllowedMentions(Collections.emptyList()).queue();
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
                    sendViewEmbeds(autoMessageCommands, requestContext);
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
            requestContext.event().getChannel().sendMessage("The command must follow this format `" + requestContext.command().getFullDescription(requestContext.prefix()) + "`").queue();
        }
    }

    private void sendViewEmbeds(List<AutoMessageCommand> commands, RequestContext requestContext) {
        List<MessageEmbed> embeds = new ArrayList<>();
        embeds.add(new EmbedBuilder().setTitle("Auto-Message Channels").setColor(DISCORD_BLUE).build());

        Map<String, List<AutoMessageCommand>> channelIdToCommands = commands.stream().collect(Collectors.groupingBy(AutoMessageCommand::getChannelId));

        for (Map.Entry<String, List<AutoMessageCommand>> entry : channelIdToCommands.entrySet()) {
            GuildChannel guildChannel = requestContext.event().getGuild().getGuildChannelById(entry.getKey());
            if (guildChannel == null) {
                continue;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            MessageEmbed embed = embedBuilder.setTitle(String.format("#%s [%s]", guildChannel.getName(), guildChannel.getId()))
                    .setColor(DISCORD_BLUE)
                    .setDescription(getCommandDisplays(entry.getValue()))
                    .build();
            embeds.add(embed);
        }

        requestContext.event().getChannel().sendMessageEmbeds(embeds).setAllowedMentions(Collections.emptyList()).queue();
    }

    private String getCommandDisplays(List<AutoMessageCommand> commands) {
        String commandDisplayTemplate = """
                Every %s %s
                **"%s"**""";

        List<String> commandDisplays = new ArrayList<>();
        for (AutoMessageCommand command : commands) {
            boolean hours = command.getMinutes() % 60 == 0;
            Integer time = hours ? command.getMinutes() / 60 : command.getMinutes();
            String timeDisplay = hours ? "hour(s)" : "minutes";
            commandDisplays.add(String.format(commandDisplayTemplate, time, timeDisplay, command.getMessage()));
        }
        return String.join("\n\n", commandDisplays);
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
