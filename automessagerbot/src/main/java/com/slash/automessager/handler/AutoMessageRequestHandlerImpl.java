package com.slash.automessager.handler;

import com.slash.automessager.Application;
import com.slash.automessager.domain.*;
import com.slash.automessager.exception.InvalidPermissionException;
import com.slash.automessager.request.RequestContext;
import com.slash.automessager.service.AutoMessageBotService;
import com.slash.automessager.service.AutoMessageBotServiceImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AutoMessageRequestHandlerImpl implements AutoMessageRequestHandler {

    private static final Color DISCORD_BLUE = Color.decode("#5566f2");

    private final AutoMessageBotService botService;

    public AutoMessageRequestHandlerImpl() {
        this(new AutoMessageBotServiceImpl());
    }

    public AutoMessageRequestHandlerImpl(AutoMessageBotService botService) {
        this.botService = botService;
    }

    @Override
    public void handleSetupCommand(RequestContext requestContext) {
        try {
            if (requestContext.getMember() == null || !canManageChannels(requestContext)) {
                throw new InvalidPermissionException("You do not have permission to add auto message commands");
            }

            String channelId = requestContext.getArgument("channel", String.class);
            GuildChannel guildChannel = channelId != null ? requestContext.getGuild().getTextChannelById(channelId) : null;
            if (guildChannel == null) {
                throw new IllegalArgumentException("Invalid channel supplied");
            }

            String time = requestContext.getArgument("time", String.class);
            if (time == null || time.isBlank()) {
                throw new IllegalArgumentException("Invalid time supplied");
            }
            int minutes;
            try {
                if (time.endsWith("h")) {
                    minutes = Integer.parseInt(time.substring(0, time.length() - 1)) * 60;
                }
                else if (time.endsWith("m")) {
                    minutes = Integer.parseInt(time.substring(0, time.length() - 1));
                }
                else {
                    minutes = Integer.parseInt(time);
                }
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid time supplied", e);
            }

            String message = requestContext.getArgument("content", String.class);
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Invalid message supplied");
            }

            BasicGuildInfo guildInfo = botService.loadGuildInfo(requestContext.getGuild().getIdLong());
            Integer guildId;
            if (guildInfo == null) {
                AutoMessageGuild guild = new AutoMessageGuild();
                guild.setBotId(Application.getBotCache().getBot().getId());
                guild.setDiscordId(requestContext.getGuild().getIdLong());
                guildId = botService.insertGuild(guild);
            }
            else {
                guildId = guildInfo.getGuildId();
            }


            AutoMessageCommand command = new AutoMessageCommand();
            command.setBotId(Application.getBotCache().getBot().getId());
            command.setGuildId(guildId);
            command.setGuildDiscordId(requestContext.getGuild().getIdLong());
            command.setChannelDiscordId(guildChannel.getIdLong());
            command.setMinutes(minutes);
            command.setContent(message);
            command.setLastRunDate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            botService.insertCommand(command);
            sendSetupEmbed(command, guildChannel, requestContext);
        }
        catch (InvalidPermissionException e) {
            requestContext.sendMessage(e.getMessage());
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            requestContext.sendMessage("The command must follow this format `" + requestContext.getCommand().getFullDescription(requestContext.getPrefix(), false) + "`");
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
                
                To see all added channels run %sview.""", guildChannel.getName(), command.getChannelDiscordId(), time, timeDisplay, command.getContent(), requestContext.getPrefix());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":white_check_mark: Successfully added new channel")
                .setColor(DISCORD_BLUE)
                .setDescription(description)
                .build();

        requestContext.sendMessageEmbeds(embed);
    }

    @Override
    public void handleRemoveCommand(RequestContext requestContext) {
        try {
            if (requestContext.getMember() == null || !canManageChannels(requestContext)) {
                throw new InvalidPermissionException("You do not have permission to remove auto message commands");
            }
            Long channelDiscordId = requestContext.getArgument("channel", Long.class);
            if (channelDiscordId == null) {
                throw new IllegalArgumentException("Invalid channel supplied");
            }

            List<AutoMessageCommand> commands = botService.getCommandsForGuild(requestContext.getGuild().getIdLong());
            List<AutoMessageCommand> channelCommands = commands.stream().filter(c -> c.getChannelDiscordId().equals(channelDiscordId)).toList();
            if (channelCommands.isEmpty()) {
                requestContext.sendMessage("There are currently no auto message commands for that channel");
                return;
            }

            botService.deleteCommands(channelCommands.stream().map(AutoMessageCommand::getId).toList());
            sendRemoveEmbed(channelCommands, requestContext.getGuild().getGuildChannelById(channelCommands.get(0).getChannelDiscordId()), requestContext);
        }
        catch (InvalidPermissionException e) {
            requestContext.sendMessage(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            requestContext.sendMessage("The command must follow this format `" + requestContext.getCommand().getFullDescription(requestContext.getPrefix(), false) + "`");
        }
    }

    private void sendRemoveEmbed(List<AutoMessageCommand> commands, GuildChannel guildChannel, RequestContext requestContext) {
        String commandDisplays = getCommandDisplays(commands);
        String description = String.format("""
                #%s [%s]
                %s
                
                To see all added channels run %sview.""", guildChannel.getName(), guildChannel.getId(), commandDisplays, requestContext.getPrefix());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":white_check_mark: Successfully removed channel")
                .setColor(DISCORD_BLUE)
                .setDescription(description)
                .build();

        requestContext.sendMessageEmbeds(embed);
    }

    @Override
    public void handleViewCommand(RequestContext requestContext) {
        try {
            if (requestContext.getMember() == null || !canManageChannels(requestContext)) {
                throw new InvalidPermissionException("You do not have permission to view auto message commands");
            }

            List<AutoMessageCommand> commands = botService.getCommandsForGuild(requestContext.getGuild().getIdLong());
            if (commands.isEmpty()) {
                requestContext.sendMessage("There are currently no auto message commands.");
            }
            else {
                sendViewEmbeds(commands, requestContext);
            }
        }
        catch (InvalidPermissionException e) {
            requestContext.sendMessage(e.getMessage());
        }
    }

    private void sendViewEmbeds(List<AutoMessageCommand> commands, RequestContext requestContext) {
        Map<Long, List<AutoMessageCommand>> channelIdToCommands = commands.stream().collect(Collectors.groupingBy(AutoMessageCommand::getChannelDiscordId));

        List<MessageEmbed> embeds = new ArrayList<>();
        embeds.add(new EmbedBuilder().setTitle("Auto-Message Channels")
                .setDescription(String.format("**%s channels & %s messages configured**", channelIdToCommands.size(), commands.size()))
                .setColor(DISCORD_BLUE).build());


        for (Map.Entry<Long, List<AutoMessageCommand>> entry : channelIdToCommands.entrySet()) {
            GuildChannel guildChannel = requestContext.getGuild().getGuildChannelById(entry.getKey());
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

        requestContext.sendMessageEmbeds(embeds);
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
            commandDisplays.add(String.format(commandDisplayTemplate, time, timeDisplay, command.getContent()));
        }
        return String.join("\n\n", commandDisplays);
    }

    private boolean canManageChannels(RequestContext requestContext) {
        Member member = requestContext.getMember();
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
