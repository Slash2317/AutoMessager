package com.slash.automessager.request;

import java.util.Collections;
import java.util.List;

public enum Command {

    VIEW(">view", "Shows list of auto message channels"),
    SETUP(">setup", "Adds an auto message channel", List.of("channel", "time", "content")),
    REMOVE(">remove", "Removes an auto message channel", List.of("channel/command number")),
    HELP(">help", "Shows list of commands"),
    SLASH_HELP("/help", "Alternative Help command which explains we only support t! prefix.");

    private final String commandName;
    private final String description;
    private final List<String> parameters;

    Command(String commandName, String description, List<String> parameters) {
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

    public List<String> getParameters() {
        return parameters;
    }

    public static Command getCommand(String message) {
        for (Command command : Command.values()) {
            if (message.equals(command.commandName) ||
                    message.startsWith(command.commandName + " ")) {
                return command;
            }
        }
        return null;
    }

    public String getCommandFormat() {
        StringBuilder sb = new StringBuilder(commandName);

        if (!parameters.isEmpty()) {
            sb.append(" [" + String.join("] [", parameters) + "]");
        }
        return sb.toString();
    }

    public String getFullDescription() {
        return getCommandFormat() + " | " + description;
    }
}
