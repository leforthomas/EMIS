package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 28/09/2015.
 */
public class OrderDisplaySettingsChanged extends GwtEvent<OrderDisplaySettingsChangedHandler> {
    public static Type<OrderDisplaySettingsChangedHandler> TYPE = new Type<OrderDisplaySettingsChangedHandler>();

    double overlayOpacity;
    double productSelectionOpacity;

    public OrderDisplaySettingsChanged(double overlayOpacity, double productSelectionOpacity) {
        this.overlayOpacity = overlayOpacity;
        this.productSelectionOpacity = productSelectionOpacity;
    }

    public double getOverlayOpacity() {
        return overlayOpacity;
    }

    public void setOverlayOpacity(double overlayOpacity) {
        this.overlayOpacity = overlayOpacity;
    }

    public double getProductSelectionOpacity() {
        return productSelectionOpacity;
    }

    public void setProductSelectionOpacity(double productSelectionOpacity) {
        this.productSelectionOpacity = productSelectionOpacity;
    }

    public Type<OrderDisplaySettingsChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(OrderDisplaySettingsChangedHandler handler) {
        handler.onOrderDisplaySettingsChanged(this);
    }
}
