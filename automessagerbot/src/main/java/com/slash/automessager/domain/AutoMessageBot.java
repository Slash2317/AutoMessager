package com.slash.automessager.domain;

public class AutoMessageBot {

    private Integer id;
    private Long discordId;
    private Integer sentMessagesCount;

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
}
