package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface TimeChangedEventHandler extends EventHandler {
    void onTimeChanged(TimeChangedEvent event);
}
