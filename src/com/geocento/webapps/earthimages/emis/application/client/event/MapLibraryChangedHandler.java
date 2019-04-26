package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 20/02/2015.
 */
public interface MapLibraryChangedHandler extends EventHandler {
    void onMapLibraryChanged(MapLibraryChanged event);
}
