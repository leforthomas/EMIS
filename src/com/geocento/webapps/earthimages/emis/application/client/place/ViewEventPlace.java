package com.geocento.webapps.earthimages.emis.application.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class ViewEventPlace extends EMISPlace {

    public static enum TOKENS {eventid};

	private Place place;

    public ViewEventPlace() {
    }

    public ViewEventPlace(String token) {
        this.token = token;
    }

    @Prefix("event")
	public static class Tokenizer implements PlaceTokenizer<ViewEventPlace> {
        @Override
        public String getToken(ViewEventPlace place) {
            return place.getToken();
        }

        @Override
        public ViewEventPlace getPlace(String token) {
            return new ViewEventPlace(token);
        }
    }
}
