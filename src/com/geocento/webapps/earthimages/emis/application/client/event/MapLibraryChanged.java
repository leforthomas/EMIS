package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 20/02/2015.
 */
public class MapLibraryChanged extends GwtEvent<MapLibraryChangedHandler> {

    public static Type<MapLibraryChangedHandler> TYPE = new Type<MapLibraryChangedHandler>();

    private final String mapLibrary;

    public MapLibraryChanged(String mapLibrary) {
        this.mapLibrary = mapLibrary;
    }

    public String getMapLibrary() {
        return mapLibrary;
    }

    public Type<MapLibraryChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(MapLibraryChangedHandler handler) {
        handler.onMapLibraryChanged(this);
    }
}
