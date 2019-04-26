package com.geocento.webapps.earthimages.emis.application.client.event;

import com.geocento.webapps.earthimages.emis.application.share.websockets.ProductOrderNotification;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 25/09/2017.
 */
public class OrderNotification extends GwtEvent<OrderNotificationHandler> {

    public static Type<OrderNotificationHandler> TYPE = new Type<OrderNotificationHandler>();

    private final ProductOrderNotification productOrderNotification;

    public OrderNotification(ProductOrderNotification productOrderNotification) {
        this.productOrderNotification = productOrderNotification;
    }

    public ProductOrderNotification getProductOrderNotification() {
        return productOrderNotification;
    }

    public Type<OrderNotificationHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(OrderNotificationHandler handler) {
        handler.onOrderNotification(this);
    }
}
