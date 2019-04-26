package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.google.gwt.event.shared.GwtEvent;

public class MakeSample extends GwtEvent<MakeSampleHandler> {

    public static Type<MakeSampleHandler> TYPE = new Type<MakeSampleHandler>();

    private ProductOrderDTO productOrderDTO;

    public MakeSample(ProductOrderDTO productOrderDTO) {
        this.productOrderDTO = productOrderDTO;
    }

    public ProductOrderDTO getProductOrderDTO() {
        return productOrderDTO;
    }

    public Type<MakeSampleHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(MakeSampleHandler handler) {
        handler.onMakeSample(this);
    }
}
