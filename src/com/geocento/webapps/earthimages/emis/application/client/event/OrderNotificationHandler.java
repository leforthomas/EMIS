package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 25/09/2017.
 */
public interface OrderNotificationHandler extends EventHandler {
    void onOrderNotification(OrderNotification event);
}
