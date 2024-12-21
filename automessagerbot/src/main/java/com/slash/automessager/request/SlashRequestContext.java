package com.slash.automessager.request;

import com.slash.automessager.domain.AutoMessageBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Collection;
import java.util.Collections;

public class SlashRequestContext implements RequestContext {

    private SlashCommandInteractionEvent event;
    private Command command;
    private AutoMessageBot bot;

    public SlashRequestContext(SlashCommandInteractionEvent event, AutoMessageBot bot) {
        this.event = event;
        this.command = Command.getCommandByName(event.getName());
        this.bot = bot;
    }

    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public <T> T getArgument(String name, Class<T> clazz) {
        OptionMapping optionMapping = event.getInteraction().getOption(name);
        if (clazz == Integer.class) {
            return (T) (Integer) optionMapping.getAsInt();
        }
        if (clazz == Long.class) {
            return (T) (Long) optionMapping.getAsLong();
        }
        if (clazz == Boolean.class) {
            return (T) (Boolean) optionMapping.getAsBoolean();
        }
        if (clazz == String.class) {
            return (T) optionMapping.getAsString();
        }
        return null;
    }

    @Override
    public String getPrefix() {
        return "/";
    }

    @Override
    public Member getMember() {
        return event.getMember();
    }

    @Override
    public Guild getGuild() {
        return event.getGuild();
    }

    @Override
    public MessageChannelUnion getChannel() {
        return event.getChannel();
    }

    @Override
    public AutoMessageBot getBot() {
        return bot;
    }

    @Override
    public void sendMessage(String message) {
        event.reply(message).setAllowedMentions(Collections.emptyList()).queue();
    }

    @Override
    public void sendMessageEmbeds(MessageEmbed embed) {
        event.replyEmbeds(embed).setAllowedMentions(Collections.emptyList()).queue();
    }

    @Override
    public void sendMessageEmbeds(Collection<MessageEmbed> embeds) {
        event.replyEmbeds(embeds).setAllowedMentions(Collections.emptyList()).queue();
    }
}
