package com.geocento.webapps.earthimages.emis.application.client.place;

import com.google.gwt.place.shared.Place;

public class EMISPlace extends Place {

    protected String token;

    public EMISPlace() {
	}

    public EMISPlace(String token) {
        this.token = token;
    }

    public String getToken() {
		return token;
	}

}
