package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 21/04/2015.
 */
public interface UserCreatedHandler extends EventHandler {
    void onUserCreated(UserCreated event);
}
