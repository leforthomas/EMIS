package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AILogsPlace extends AIPlace {

    public static enum TOKENS {};

	private Place place;

    public AILogsPlace() {
    }

    public AILogsPlace(String token) {
        this.token = token;
    }

    public AILogsPlace(String token, Place place) {
        this.token = token;
        this.place = place;
    }

    public Place getPlace() {
		return place;
	}

    @Prefix("logging")
	public static class Tokenizer implements PlaceTokenizer<AILogsPlace> {
        @Override
        public String getToken(AILogsPlace place) {
            return place.getToken();
        }

        @Override
        public AILogsPlace getPlace(String token) {
            return new AILogsPlace(token);
        }
    }
}
