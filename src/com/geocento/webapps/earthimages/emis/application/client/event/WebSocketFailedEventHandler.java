package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 28/06/2017.
 */
public interface WebSocketFailedEventHandler extends EventHandler {
    void onWebSocketFailed(WebSocketFailedEvent event);
}
