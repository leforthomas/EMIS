package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.google.gwt.event.shared.GwtEvent;

public class DeleteSample extends GwtEvent<DeleteSampleHandler> {

    public static Type<DeleteSampleHandler> TYPE = new Type<DeleteSampleHandler>();

    private SampleDTO sampleDTO;

    public DeleteSample(SampleDTO sampleDTO) {
        this.sampleDTO = sampleDTO;
    }

    public SampleDTO getSampleDTO() {
        return sampleDTO;
    }

    public Type<DeleteSampleHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(DeleteSampleHandler handler) {
        handler.onDeleteSample(this);
    }
}
