package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AIOrdersPlace extends AIPlace {

    public static enum TOKENS {};

	private Place place;

    public AIOrdersPlace() {
    }

    public AIOrdersPlace(String token) {
        this.token = token;
    }

    public AIOrdersPlace(String token, Place place) {
        this.token = token;
        this.place = place;
    }

    public Place getPlace() {
		return place;
	}

    @Prefix("orders")
	public static class Tokenizer implements PlaceTokenizer<AIOrdersPlace> {
        @Override
        public String getToken(AIOrdersPlace place) {
            return place.getToken();
        }

        @Override
        public AIOrdersPlace getPlace(String token) {
            return new AIOrdersPlace(token);
        }
    }
}
