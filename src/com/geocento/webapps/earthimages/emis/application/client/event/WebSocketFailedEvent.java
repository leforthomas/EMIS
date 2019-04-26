package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 28/06/2017.
 */
public class WebSocketFailedEvent extends GwtEvent<WebSocketFailedEventHandler> {

    public static Type<WebSocketFailedEventHandler> TYPE = new Type<WebSocketFailedEventHandler>();

    public Type<WebSocketFailedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(WebSocketFailedEventHandler handler) {
        handler.onWebSocketFailed(this);
    }
}
