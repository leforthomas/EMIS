package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.google.gwt.event.shared.GwtEvent;

public class UploadProduct extends GwtEvent<UploadProductHandler> {

    public static Type<UploadProductHandler> TYPE = new Type<UploadProductHandler>();

    private final ProductOrderDTO productOrder;

    public UploadProduct(ProductOrderDTO productOrderDTO) {
        this.productOrder = productOrderDTO;
    }

    public ProductOrderDTO getProductOrder() {
        return productOrder;
    }

    public Type<UploadProductHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(UploadProductHandler handler) {
        handler.onUploadProduct(this);
    }
}
