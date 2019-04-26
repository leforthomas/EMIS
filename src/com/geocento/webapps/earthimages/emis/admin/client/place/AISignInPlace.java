package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AISignInPlace extends AIPlace {

    public static enum TOKENS {signIn, signUp, registration};

	private Place place;

    public AISignInPlace() {
    }

    public AISignInPlace(String token) {
        this.token = token;
    }

    public AISignInPlace(String token, Place place) {
        this.token = token;
        this.place = place;
    }

    public Place getPlace() {
		return place;
	}

    @Prefix("signin")
	public static class Tokenizer implements PlaceTokenizer<AISignInPlace> {
        @Override
        public String getToken(AISignInPlace place) {
            return place.getToken();
        }

        @Override
        public AISignInPlace getPlace(String token) {
            return new AISignInPlace(token);
        }
    }
}
