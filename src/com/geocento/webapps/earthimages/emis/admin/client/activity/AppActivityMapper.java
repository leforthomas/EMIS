package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.place.*;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class AppActivityMapper implements ActivityMapper {
    private ClientFactory clientFactory;

    public AppActivityMapper(ClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
    	if (place instanceof AISignInPlace) {
            return new AISignInActivity((AISignInPlace) place, clientFactory);
        } else if (place instanceof AISettingsPlace) {
            return new AISettingsActivity((AISettingsPlace) place, clientFactory);
        } else if (place instanceof AIUsersPlace) {
            return new AIUsersActivity((AIUsersPlace) place, clientFactory);
        } else if (place instanceof AIPublishPlace) {
            return new AIPublishActivity((AIPublishPlace) place, clientFactory);
        } else if (place instanceof AIOrdersPlace) {
            return new AIOrdersActivity((AIOrdersPlace) place, clientFactory);
        } else if (place instanceof AISamplesPlace) {
            return new AISamplesActivity((AISamplesPlace) place, clientFactory);
        } else if (place instanceof AILogsPlace) {
            return new AILogsActivity((AILogsPlace) place, clientFactory);
        }
        return null;
    }

}
