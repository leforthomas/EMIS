package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 13/10/2017.
 */
public interface ProductOrderChangedHandler extends EventHandler {
    void onProductOrderChanged(ProductOrderChanged event);
}
