package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.google.gwt.event.shared.GwtEvent;

public class ChangeOrder extends GwtEvent<ChangeOrderHandler> {
    public static Type<ChangeOrderHandler> TYPE = new Type<ChangeOrderHandler>();

    private final ProductOrderDTO productOrder;

    public ChangeOrder(ProductOrderDTO productOrderDTO) {
        this.productOrder = productOrderDTO;
    }

    public ProductOrderDTO getProductOrder() {
        return productOrder;
    }

    public Type<ChangeOrderHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ChangeOrderHandler handler) {
        handler.onChangeOrder(this);
    }
}
