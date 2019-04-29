package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class ProductOrderPlaceChange extends GwtEvent<ProductOrderPlaceChangeHandler> {

    public static Type<ProductOrderPlaceChangeHandler> TYPE = new Type<ProductOrderPlaceChangeHandler>();

    private final String targetProductId;
    private final String productId;

    public ProductOrderPlaceChange(String targetProductId, String productId) {
        this.targetProductId = targetProductId;
        this.productId = productId;
    }

    public String getTargetProductId() {
        return targetProductId;
    }

    public String getProductId() {
        return productId;
    }

    public Type<ProductOrderPlaceChangeHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ProductOrderPlaceChangeHandler handler) {
        handler.onProductOrderPlaceChange(this);
    }
}
