package com.geocento.webapps.earthimages.emis.application.client.event;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 20/04/2015.
 */
public class AddAoIImported extends GwtEvent<AddAoIImportedHandler> {

    public static Type<AddAoIImportedHandler> TYPE = new Type<AddAoIImportedHandler>();

    private final AOI aoi;

    public AddAoIImported(AOI aoi) {
        this.aoi = aoi;
    }

    public AOI getAoi() {
        return aoi;
    }

    public Type<AddAoIImportedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(AddAoIImportedHandler handler) {
        handler.onAddAoIImported(this);
    }
}
