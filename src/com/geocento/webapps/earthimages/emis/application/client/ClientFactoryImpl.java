package com.geocento.webapps.earthimages.emis.application.client;

import com.geocento.webapps.earthimages.emis.application.client.place.EventsPlace;
import com.geocento.webapps.earthimages.emis.application.client.views.*;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public class ClientFactoryImpl implements ClientFactory {
	
    private final EventBus eventBus = new SimpleEventBus();

    private final PlaceController placeController = new PlaceController(eventBus);

    private EventsViewImpl ordersView = null;

    private ViewEventViewImpl orderingView = null;

    private SignInViewImpl signInView = null;

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }

    @Override
    public Place getDefaultPlace() {
        return new EventsPlace();
    }

    @Override
    public EventsView getOrdersView() {
        if(ordersView == null) {
            ordersView = new EventsViewImpl(this);
        }
        return ordersView;
    }

    @Override
    public ViewEventView getViewEventView() {
        if(orderingView == null) {
            orderingView = new ViewEventViewImpl(this);
        }
        return orderingView;
    }

    @Override
    public SignInView getSignInView() {
        if(signInView == null) {
            signInView = new SignInViewImpl(this);
        }
        return signInView;
    }

}
