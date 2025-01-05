package com.slash.automessager.domain;

import java.time.LocalDateTime;

public class AutoMessageCommand {

    private Integer id;
    private Integer botId;
    private Integer guildId;
    private Long guildDiscordId;
    private Long channelDiscordId;
    private Integer minutes;
    private String content;
    private LocalDateTime lastRunDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBotId() {
        return botId;
    }

    public void setBotId(Integer botId) {
        this.botId = botId;
    }

    public Integer getGuildId() {
        return guildId;
    }

    public void setGuildId(Integer guildId) {
        this.guildId = guildId;
    }

    public Long getGuildDiscordId() {
        return guildDiscordId;
    }

    public void setGuildDiscordId(Long guildDiscordId) {
        this.guildDiscordId = guildDiscordId;
    }

    public Long getChannelDiscordId() {
        return channelDiscordId;
    }

    public void setChannelDiscordId(Long channelDiscordId) {
        this.channelDiscordId = channelDiscordId;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getLastRunDate() {
        return lastRunDate;
    }

    public void setLastRunDate(LocalDateTime lastRunDate) {
        this.lastRunDate = lastRunDate;
    }
}
