package com.slash.automessager.handler;

import com.slash.automessager.request.RequestContext;

public interface MiscRequestHandler {
    void handleHelpCommand(RequestContext requestContext);
    void handlePrefixCommand(RequestContext requestContext);
    void handleVoteCommand(RequestContext requestContext);
}
