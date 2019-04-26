package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created by thomas on 28/09/2015.
 */
public interface OrderDisplaySettingsChangedHandler extends EventHandler {
    void onOrderDisplaySettingsChanged(OrderDisplaySettingsChanged event);
}
