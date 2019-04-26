package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AIUsersPlace extends AIPlace {

    public static enum TOKENS {};

	private Place place;

    public AIUsersPlace() {
    }

    public AIUsersPlace(String token) {
        this.token = token;
    }

    public AIUsersPlace(String token, Place place) {
        this.token = token;
        this.place = place;
    }

    public Place getPlace() {
		return place;
	}

    @Prefix("users")
	public static class Tokenizer implements PlaceTokenizer<AIUsersPlace> {
        @Override
        public String getToken(AIUsersPlace place) {
            return place.getToken();
        }

        @Override
        public AIUsersPlace getPlace(String token) {
            return new AIUsersPlace(token);
        }
    }
}
