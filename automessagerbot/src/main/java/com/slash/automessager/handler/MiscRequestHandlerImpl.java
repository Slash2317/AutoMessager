package com.slash.automessager.handler;

import com.slash.automessager.domain.AutoMessageBot;
import com.slash.automessager.domain.AutoMessageGuild;
import com.slash.automessager.exception.InvalidPermissionException;
import com.slash.automessager.request.Command;
import com.slash.automessager.request.RequestContext;
import com.slash.automessager.service.AutoMessageBotService;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MiscRequestHandlerImpl implements MiscRequestHandler {

    private static final Color DISCORD_BLUE = Color.decode("#5566f2");

    private final AutoMessageBotService botService;

    @Autowired
    public MiscRequestHandlerImpl(AutoMessageBotService botService) {
        this.botService = botService;
    }

    @Override
    public void handleHelpCommand(RequestContext requestContext) {
        String commandsDisplay = Arrays.stream(Command.values()).map(c -> c.getFullDescription(requestContext.getPrefix(), true)).collect(Collectors.joining("\n"));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        MessageEmbed embed = embedBuilder.setTitle(":blue_circle: AUTO-MESSAGER | COMMANDS")
                .setColor(DISCORD_BLUE)
                .setDescription(commandsDisplay)
                .build();

        requestContext.sendMessageEmbeds(embed);
    }

    @Override
    @Transactional
    public void handlePrefixCommand(RequestContext requestContext) {
        try {
            if (requestContext.getMember() == null || !canManageChannels(requestContext)) {
                throw new InvalidPermissionException("You do not have permission to remove auto message commands");
            }
            String prefix = requestContext.getArgument("prefix", String.class);
            if (prefix == null || prefix.isBlank()) {
                throw new IllegalArgumentException("No prefix supplied");
            }
            if (prefix.trim().equals("/")) {
                requestContext.sendMessage("Invalid prefix supplied");
                return;
            }
            if (prefix.length() >= 20) {
                requestContext.sendMessage("Prefix must be at most 20 characters");
                return;
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

            guild.setPrefix(prefix);
            botService.save(bot);

            EmbedBuilder embedBuilder = new EmbedBuilder();
            MessageEmbed embed = embedBuilder.setTitle(":white_check_mark: Successfully updated prefix to " + prefix)
                    .setColor(DISCORD_BLUE)
                    .build();

            requestContext.sendMessageEmbeds(embed);
        }
        catch (InvalidPermissionException e) {
            requestContext.sendMessage(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            requestContext.sendMessage("The command must follow this format `" + requestContext.getCommand().getFullDescription(requestContext.getPrefix(), false) + "`");
        }
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
