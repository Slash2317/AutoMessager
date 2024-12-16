package com.slash.automessager.domain;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.*;

public class Data {

    private Map<String, List<AutoMessageCommand>> guildIdToAutoMessageCommands = new HashMap<>();
    private Map<String, String> guildIdToPrefix = new HashMap<>();
    private Integer messagesSent = 0;

    public Map<String, List<AutoMessageCommand>> getGuildIdToAutoMessageCommands() {
        return guildIdToAutoMessageCommands;
    }

    public void setGuildIdToAutoMessageCommands(Map<String, List<AutoMessageCommand>> guildIdToAutoMessageCommands) {
        this.guildIdToAutoMessageCommands = guildIdToAutoMessageCommands;
    }

    public Map<String, String> getGuildIdToPrefix() {
        return guildIdToPrefix;
    }

    public void setGuildIdToPrefix(Map<String, String> guildIdToPrefix) {
        this.guildIdToPrefix = guildIdToPrefix;
    }

    public Integer getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(Integer messagesSent) {
        this.messagesSent = messagesSent;
    }

    public String getAutoMessageCommandsDisplay(Guild guild) {
        List<String> channelIds = guildIdToAutoMessageCommands.get(guild.getId()).stream().map(AutoMessageCommand::getChannelId).toList();
        LinkedHashMap<String, GuildChannel> idToChannel = getChannels(channelIds, guild);

        List<String> displays = new ArrayList<>();
        for (AutoMessageCommand command : guildIdToAutoMessageCommands.get(guild.getId())) {
            boolean hours = command.getMinutes() % 60 == 0;
            Integer time = hours ? command.getMinutes() / 60 : command.getMinutes();
            String timeDisplay = hours ? "hour(s)" : "minutes";

            displays.add(String.format("""
                    Channel %s (%s)
                    Every %s %s
                    %s""", idToChannel.get(command.getChannelId()), command.getChannelId(), time, timeDisplay, command.getMessage()));
        }

        return String.join("\n\n", displays);
    }

    private LinkedHashMap<String, GuildChannel> getChannels(List<String> channelIds, Guild guild) {
        LinkedHashMap<String, GuildChannel> idToChannel = new LinkedHashMap<>();
        for (String id : channelIds) {
            idToChannel.put(id, guild.getGuildChannelById(id));
        }
        return idToChannel;
    }
}
