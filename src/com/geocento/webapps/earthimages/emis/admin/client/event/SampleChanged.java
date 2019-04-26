package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.google.gwt.event.shared.GwtEvent;

public class SampleChanged extends GwtEvent<SampleChangedHandler> {

    public static Type<SampleChangedHandler> TYPE = new Type<SampleChangedHandler>();

    private SampleDTO sample;

    public SampleChanged(SampleDTO sample) {
        this.sample = sample;
    }

    public SampleDTO getSample() {
        return sample;
    }

    public Type<SampleChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(SampleChangedHandler handler) {
        handler.onSampleChanged(this);
    }
}
