package com.slash.automessager.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AutoMessageGuild {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AutoMessageGuildID")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "AutoMessageBotID")
    private AutoMessageBot bot;

    @Column(name = "GuildDiscordID")
    private Long discordId;

    private String prefix;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "AutoMessageGuildID", insertable = false, updatable = false)
    private List<AutoMessageCommand> commands = new ArrayList<>();

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

    public Long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(Long discordId) {
        this.discordId = discordId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<AutoMessageCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<AutoMessageCommand> commands) {
        this.commands = commands;
    }
}
