package com.geocento.webapps.earthimages.emis.admin.client;

import com.geocento.webapps.earthimages.emis.admin.client.place.AISettingsPlace;
import com.geocento.webapps.earthimages.emis.admin.client.view.*;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public class ClientFactoryImpl implements ClientFactory {
	
    private final EventBus eventBus = new SimpleEventBus();

    private final PlaceController placeController = new PlaceController(eventBus);

    private AISignInViewImpl aiSignInView = null;

    private AISettingsViewImpl aiSettingsView = null;

    private AIPublishViewImpl aiPublishView = null;

    private AIOrdersViewImpl aiOrdersView = null;

    private AISamplesViewImpl aiSamplesView = null;

    private AIUsersViewImpl aiUsersView = null;

    private AILogsViewImpl aiLogsView = null;

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
        return new AISettingsPlace();
    }

    @Override
    public AISignInView getAISignInView() {
        if(aiSignInView == null) {
            aiSignInView = new AISignInViewImpl(this);
        }
        return aiSignInView;
    }

    @Override
    public AISettingsView getAISettingsView() {
        if(aiSettingsView == null) {
            aiSettingsView = new AISettingsViewImpl(this);
        }
        return aiSettingsView;
    }

    @Override
    public AILogsView getAILogsView() {
        if(aiLogsView == null) {
            aiLogsView = new AILogsViewImpl(this);
        }
        return aiLogsView;
    }

    @Override
    public AIPublishView getAIPublishView() {
        if(aiPublishView == null) {
            aiPublishView = new AIPublishViewImpl(this);
        }
        return aiPublishView;
    }

    @Override
    public AIUsersView getAIUsersView() {
        if(aiUsersView == null) {
            aiUsersView = new AIUsersViewImpl(this);
        }
        return aiUsersView;
    }

    @Override
    public AIOrdersView getAIOrdersView() {
        if(aiOrdersView == null) {
            aiOrdersView = new AIOrdersViewImpl(this);
        }
        return aiOrdersView;
    }

    @Override
    public AISamplesView getAISamplesView() {
        if(aiSamplesView == null) {
            aiSamplesView = new AISamplesViewImpl(this);
        }
        return aiSamplesView;
    }

}
