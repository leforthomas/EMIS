package com.geocento.webapps.earthimages.emis.application.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class EventsPlace extends EMISPlace {

    public static enum TOKENS {filterText, start, stop};

	private Place place;

    public EventsPlace() {
    }

    public EventsPlace(String token) {
        this.token = token;
    }

    @Prefix("orders")
	public static class Tokenizer implements PlaceTokenizer<EventsPlace> {
        @Override
        public String getToken(EventsPlace place) {
            return place.getToken();
        }

        @Override
        public EventsPlace getPlace(String token) {
            return new EventsPlace(token);
        }
    }
}
