package com.slash.automessager.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AutoMessageBot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AutoMessageBotID")
    private Integer id;

    @Column(name = "BotDiscordID")
    private Long discordId;

    private Integer sentMessagesCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "AutoMessageBotID", insertable = false, updatable = false)
    private List<AutoMessageGuild> guilds = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    public Integer getSentMessagesCount() {
        return sentMessagesCount;
    }

    public void setSentMessagesCount(Integer sentMessagesCount) {
        this.sentMessagesCount = sentMessagesCount;
    }

    public List<AutoMessageGuild> getGuilds() {
        return guilds;
    }

    public void setGuilds(List<AutoMessageGuild> guilds) {
        this.guilds = guilds;
    }
}
