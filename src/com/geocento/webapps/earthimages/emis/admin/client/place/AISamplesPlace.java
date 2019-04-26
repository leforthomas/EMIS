package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AISamplesPlace extends AIPlace {

    public static enum TOKENS {};

	private Place place;

    public AISamplesPlace() {
    }

    public AISamplesPlace(String token) {
        this.token = token;
    }

    public AISamplesPlace(String token, Place place) {
        this.token = token;
        this.place = place;
    }

    public Place getPlace() {
		return place;
	}

    @Prefix("samples")
	public static class Tokenizer implements PlaceTokenizer<AISamplesPlace> {
        @Override
        public String getToken(AISamplesPlace place) {
            return place.getToken();
        }

        @Override
        public AISamplesPlace getPlace(String token) {
            return new AISamplesPlace(token);
        }
    }
}
