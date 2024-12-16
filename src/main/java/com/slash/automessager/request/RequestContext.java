package com.slash.automessager.request;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public record RequestContext(Command command, String arguments, MessageReceivedEvent event) {

    public static RequestContext from(MessageReceivedEvent event) {
        Command command = Command.getCommand(event.getMessage().getContentRaw());
        if (command != null) {
            if (event.getMessage().getContentRaw().length() > command.getCommandName().length() + 1) {
                String arguments = event.getMessage().getContentRaw().substring(command.getCommandName().length() + 1);
                return new RequestContext(command, arguments, event);
            }
            return new RequestContext(command, null, event);
        }
        return new RequestContext(null, null, event);
    }
}
