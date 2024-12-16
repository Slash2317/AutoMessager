package com.slash.automessager.request;

import java.util.Collections;
import java.util.List;

public enum Command {

    SETUP(">setup", "Add a new channel to automate messages in.", List.of("channel", "time", "content")),
    REMOVE(">remove", "Stop automating messages in the selected channel.", List.of("channel")),
    VIEW(">view", "View all channels where the bot is automating messages at."),
    HELP(">help", "View all the bot commands.");

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
        return "**" + getCommandFormat() + "**" + " | " + description;
    }
}
