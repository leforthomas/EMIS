package com.geocento.webapps.earthimages.emis.application.client.event;

import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.google.gwt.event.shared.GwtEvent;

public class PublishProductOrder extends GwtEvent<PublishProductOrderHandler> {

    public static Type<PublishProductOrderHandler> TYPE = new Type<PublishProductOrderHandler>();

    private final ProductOrderDTO productOrder;

    public PublishProductOrder(ProductOrderDTO productOrder) {
        this.productOrder = productOrder;
    }

    public ProductOrderDTO getProductOrder() {
        return productOrder;
    }

    public Type<PublishProductOrderHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(PublishProductOrderHandler handler) {
        handler.onPublishProductOrder(this);
    }
}
