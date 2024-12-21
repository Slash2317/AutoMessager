package com.slash.automessager.handler;

import com.slash.automessager.domain.*;
import com.slash.automessager.exception.InvalidPermissionException;
import com.slash.automessager.request.RequestContext;
import com.slash.automessager.service.AutoMessageBotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AutoMessageRequestHandlerImpl implements AutoMessageRequestHandler {

    private static final Color DISCORD_BLUE = Color.decode("#5566f2");

    private final AutoMessageBotService botService;

    @Autowired
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
            Long guildId = requestContext.getGuild().getIdLong();

            AutoMessageBot bot = botService.getCurrentBot();
            AutoMessageGuild guild = bot.getGuilds().stream().filter(g -> g.getDiscordId().equals(guildId)).findFirst().orElse(null);
            if (guild == null) {
                guild = new AutoMessageGuild();
                guild.setBot(bot);
                guild.setDiscordId(guildId);
                bot.getGuilds().add(guild);
            }

            AutoMessageCommand command = new AutoMessageCommand();
            command.setBot(bot);
            command.setGuild(guild);
            command.setGuildDiscordId(guildId);
            command.setChannelDiscordId(guildChannel.getIdLong());
            command.setMinutes(minutes);
            command.setContent(message);
            command.setLastRunDate(LocalDateTime.now());
            guild.getCommands().add(command);
            botService.save(bot);
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

            AutoMessageBot bot = botService.getCurrentBot();
            AutoMessageGuild guild = bot.getGuilds().stream().filter(g -> g.getDiscordId().equals(requestContext.getGuild().getIdLong())).findFirst().orElse(null);
            if (guild == null || guild.getCommands().isEmpty()) {
                requestContext.sendMessage("There are currently no auto message commands");
                return;
            }

            Long channelId = requestContext.getArgument("channel", Long.class);
            if (channelId == null) {
                throw new IllegalArgumentException("Invalid channel supplied");
            }
            List<AutoMessageCommand> commandsToRemove = new ArrayList<>();
            for (AutoMessageCommand command : guild.getCommands()) {
                if (command.getChannelDiscordId().equals(channelId)) {
                    commandsToRemove.add(command);
                }
            }
            if (!commandsToRemove.isEmpty()) {
                guild.getCommands().removeAll(commandsToRemove);
                if (guild.getCommands().isEmpty() && guild.getPrefix() == null) {
                    bot.getGuilds().remove(guild);
                }
                botService.save(bot);

                sendRemoveEmbed(commandsToRemove, requestContext.getGuild().getGuildChannelById(commandsToRemove.getFirst().getChannelDiscordId()), requestContext);
            }
            else {
                requestContext.sendMessage("There are currently no auto message commands for that channel");
            }
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
            String guildId = requestContext.getGuild().getId();
            AutoMessageBot bot = botService.getCurrentBot();
            AutoMessageGuild guild = bot.getGuilds().stream().filter(g -> g.getDiscordId().equals(requestContext.getGuild().getIdLong())).findFirst().orElse(null);
            if (guild != null) {
                List<AutoMessageCommand> autoMessageCommands = guild.getCommands();
                if (autoMessageCommands.isEmpty()) {
                    requestContext.sendMessage("There are currently no auto message commands.");
                }
                else {
                    sendViewEmbeds(autoMessageCommands, requestContext);
                }
            }
            else {
                requestContext.sendMessage("There are currently no auto message commands.");
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
