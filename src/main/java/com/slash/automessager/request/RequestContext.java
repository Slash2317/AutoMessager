package com.slash.automessager.request;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;

import java.util.Collection;

public interface RequestContext {
    Command getCommand();
    <T> T getArgument(String name, Class<T> clazz);
    String getPrefix();
    Member getMember();
    Guild getGuild();
    MessageChannelUnion getChannel();
    void sendMessage(String message);
    void sendMessageEmbeds(MessageEmbed embed);
    void sendMessageEmbeds(Collection<MessageEmbed> embeds);
}
