package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.metaaps.webapps.libraries.client.map.EOBounds;

/**
 * Created by thomas on 16/02/2015.
 */
public class LocationSelected extends GwtEvent<LocationSelectedHandler> {

    public static Type<LocationSelectedHandler> TYPE = new Type<LocationSelectedHandler>();

    private String placeName;
    private EOBounds bounds;

    public LocationSelected(String placeName, EOBounds bounds) {
        this.placeName = placeName;
        this.bounds = bounds;
    }

    public String getPlaceName() {
        return placeName;
    }

    public EOBounds getBounds() {
        return bounds;
    }

    public Type<LocationSelectedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(LocationSelectedHandler handler) {
        handler.onLocationSelected(this);
    }
}
