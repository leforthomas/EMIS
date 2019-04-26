package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.google.gwt.event.shared.GwtEvent;

public class EditSample extends GwtEvent<EditSampleHandler> {

    public static Type<EditSampleHandler> TYPE = new Type<EditSampleHandler>();

    private SampleDTO sampleDTO;

    public EditSample(SampleDTO sampleDTO) {
        this.sampleDTO = sampleDTO;
    }

    public SampleDTO getSampleDTO() {
        return sampleDTO;
    }

    public Type<EditSampleHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(EditSampleHandler handler) {
        handler.onEditSample(this);
    }
}
