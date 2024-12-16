package com.slash.automessager.handler;

import com.slash.automessager.request.Command;
import com.slash.automessager.request.RequestContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class MiscRequestHandlerImpl implements MiscRequestHandler {

    @Override
    public void handleHelpCommand(RequestContext requestContext) {
        String commandsDisplay = Arrays.stream(Command.values()).map(Command::getFullDescription).collect(Collectors.joining("\n"));
        requestContext.event().getChannel().sendMessage(commandsDisplay).queue();
    }
}
