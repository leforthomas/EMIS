package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;

public class AIPlace extends Place {

    protected String token;

    public AIPlace() {
	}

    public AIPlace(String token) {
        this.token = token;
    }

    public String getToken() {
		return token;
	}

}
