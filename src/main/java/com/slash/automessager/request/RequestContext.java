package com.slash.automessager.request;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public record RequestContext(Command command, String arguments, String prefix, MessageReceivedEvent event) {

    public static RequestContext from(MessageReceivedEvent event, String prefix) {
        Command command = Command.getCommand(event.getMessage().getContentRaw(), prefix);
        if (command != null) {
            if (event.getMessage().getContentRaw().length() > command.getCommandName().length() + 1 + prefix.length()) {
                String arguments = event.getMessage().getContentRaw().substring(command.getCommandName().length() + 1 + prefix.length());
                return new RequestContext(command, arguments, prefix, event);
            }
            return new RequestContext(command, null, prefix, event);
        }
        return new RequestContext(null, null, prefix, event);
    }
}
