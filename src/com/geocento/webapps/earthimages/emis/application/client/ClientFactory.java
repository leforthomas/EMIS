package com.geocento.webapps.earthimages.emis.application.client;

import com.geocento.webapps.earthimages.emis.application.client.views.*;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public interface ClientFactory {

    EventBus getEventBus();

    PlaceController getPlaceController();
    
    Place getDefaultPlace();

    EventsView getOrdersView();

    ViewEventView getViewEventView();

    SignInView getSignInView();
}
