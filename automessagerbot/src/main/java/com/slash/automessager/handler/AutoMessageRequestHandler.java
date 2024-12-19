package com.slash.automessager.handler;

import com.slash.automessager.request.RequestContext;

public interface AutoMessageRequestHandler {
    void handleSetupCommand(RequestContext requestContext);
    void handleRemoveCommand(RequestContext requestContext);
    void handleViewCommand(RequestContext requestContext);
}
