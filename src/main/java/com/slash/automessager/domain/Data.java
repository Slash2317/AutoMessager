package com.slash.automessager.domain;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.*;

public class Data {

    private Map<String, List<AutoMessageCommand>> guildIdToAutoMessageCommands = new HashMap<>();

    public Map<String, List<AutoMessageCommand>> getGuildIdToAutoMessageCommands() {
        return guildIdToAutoMessageCommands;
    }

    public void setGuildIdToAutoMessageCommands(Map<String, List<AutoMessageCommand>> guildIdToAutoMessageCommands) {
        this.guildIdToAutoMessageCommands = guildIdToAutoMessageCommands;
    }

    public String getAutoMessageCommandsDisplay(Guild guild) {
        List<String> channelIds = guildIdToAutoMessageCommands.get(guild.getId()).stream().map(AutoMessageCommand::getChannelId).toList();
        LinkedHashMap<String, GuildChannel> idToChannel = getChannels(channelIds, guild);

        List<String> displays = new ArrayList<>();
        for (AutoMessageCommand command : guildIdToAutoMessageCommands.get(guild.getId())) {
            displays.add("[Channel " + idToChannel.get(command.getChannelId()) + " (" + command.getChannelId() + ")\n" +
                    "Every " + command.getMinutes() + " minutes");
            displays.add(String.format("""
                    Channel %s (%s)
                    Every %s minutes
                    %s""", idToChannel.get(command.getChannelId()), command.getChannelId(), command.getMinutes(), command.getMessage()));
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
