package com.slash.automessager.request;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Command {

    SETUP("setup", "Add a new channel to automate messages in.",
            List.of(new OptionInfo("channel", "The channel to send the auto message in", OptionType.CHANNEL),
                    new OptionInfo("time", "How often the message should be sent e.g. 10m, 2h etc.", OptionType.STRING),
                    new OptionInfo("content", "The message that should be sent", OptionType.STRING))),
    REMOVE("remove", "Stop automating messages in the selected channel.",
            List.of(new OptionInfo("channel", "The channel to remove the auto message from", OptionType.CHANNEL))),
    VIEW("view", "View all channels where the bot is automating messages at."),
    PREFIX("prefix", "Changes the prefix for all bot commands.",
            List.of(new OptionInfo("prefix", "The prefix for all commands to use (does not affect / commands)", OptionType.STRING))),
    VOTE("vote", "Posts the top.gg link to vote for the bot"),
    HELP("help", "View all the bot commands.");

    private final String commandName;
    private final String description;
    private final List<OptionInfo> parameters;

    Command(String commandName, String description, List<OptionInfo> parameters) {
        this.commandName = commandName;
        this.description = description;
        this.parameters = parameters;
    }

    Command(String commandName, String description) {
        this(commandName, description, Collections.emptyList());
    }


    public String getCommandName() {
        return commandName;
    }

    public String getDescription() {
        return description;
    }

    public List<OptionInfo> getParameters() {
        return parameters;
    }

    public static Command getCommandByMessage(String message, String prefix) {
        for (Command command : Command.values()) {
            if (message.equals(prefix + command.commandName) ||
                    message.startsWith(prefix + command.commandName + " ")) {
                return command;
            }
        }
        return null;
    }

    public static Command getCommandByName(String name) {
        String lowercaseName = name.toLowerCase();
        return Arrays.stream(Command.values()).filter(c -> c.commandName.equals(lowercaseName)).findFirst().orElse(null);
    }

    public String getCommandFormat(String prefix) {
        StringBuilder sb = new StringBuilder(prefix + commandName);

        if (!parameters.isEmpty()) {
            sb.append(" [" + String.join("] [", parameters.stream().map(OptionInfo::name).toList()) + "]");
        }
        return sb.toString();
    }

    public String getFullDescription(String prefix, boolean bold) {
        if (bold) {
            return "**" + getCommandFormat(prefix) + "** | " + description;
        }
        return getCommandFormat(prefix) + " | " + description;
    }
}
