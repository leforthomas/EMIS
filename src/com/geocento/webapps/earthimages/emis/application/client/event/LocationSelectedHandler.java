package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 16/02/2015.
 */
public interface LocationSelectedHandler extends EventHandler {
    void onLocationSelected(LocationSelected event);
}
