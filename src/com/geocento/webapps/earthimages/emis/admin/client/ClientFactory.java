package com.geocento.webapps.earthimages.emis.admin.client;

import com.geocento.webapps.earthimages.emis.admin.client.view.*;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public interface ClientFactory {

    EventBus getEventBus();

    PlaceController getPlaceController();
    
    Place getDefaultPlace();

    AISignInView getAISignInView();

    AISettingsView getAISettingsView();

    AILogsView getAILogsView();

    AIPublishView getAIPublishView();

    AIUsersView getAIUsersView();

    AIOrdersView getAIOrdersView();

    AISamplesView getAISamplesView();
}
