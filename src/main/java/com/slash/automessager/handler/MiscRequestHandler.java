package com.slash.automessager.handler;

import com.slash.automessager.request.RequestContext;

public interface MiscRequestHandler {
    void handleHelpCommand(RequestContext requestContext, String prefix);
    void handlePrefixCommand(RequestContext requestContext);
}
