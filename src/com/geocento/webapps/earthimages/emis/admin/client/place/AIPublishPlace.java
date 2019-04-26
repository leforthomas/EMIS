package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AIPublishPlace extends AIPlace {

    public static enum TOKENS {};

	private Place place;

    public AIPublishPlace() {
    }

    public AIPublishPlace(String token) {
        this.token = token;
    }

    public AIPublishPlace(String token, Place place) {
        this.token = token;
        this.place = place;
    }

    public Place getPlace() {
		return place;
	}

    @Prefix("publish")
	public static class Tokenizer implements PlaceTokenizer<AIPublishPlace> {
        @Override
        public String getToken(AIPublishPlace place) {
            return place.getToken();
        }

        @Override
        public AIPublishPlace getPlace(String token) {
            return new AIPublishPlace(token);
        }
    }
}
