package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 21/01/2015.
 */
public interface UserChangedHandler extends EventHandler {
    void onUserChanged(UserChanged event);
}
