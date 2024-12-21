package com.slash.automessager.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class AutoMessageCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AutoMessageCommandID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "AutoMessageBotID")
    private AutoMessageBot bot;

    @ManyToOne
    @JoinColumn(name = "AutoMessageGuildID")
    private AutoMessageGuild guild;

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

    public AutoMessageBot getBot() {
        return bot;
    }

    public void setBot(AutoMessageBot bot) {
        this.bot = bot;
    }

    public AutoMessageGuild getGuild() {
        return guild;
    }

    public void setGuild(AutoMessageGuild guild) {
        this.guild = guild;
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
