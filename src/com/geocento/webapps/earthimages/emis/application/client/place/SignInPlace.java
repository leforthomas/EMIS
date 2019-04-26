package com.geocento.webapps.earthimages.emis.application.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class SignInPlace extends EMISPlace {

    public static enum TOKENS {};

	private Place place;

    public SignInPlace() {
    }

    public SignInPlace(String token) {
        this.token = token;
    }

    public SignInPlace(String token, Place place) {
        super(token);
        this.place = place;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    @Prefix("signIn")
	public static class Tokenizer implements PlaceTokenizer<SignInPlace> {
        @Override
        public String getToken(SignInPlace place) {
            return place.getToken();
        }

        @Override
        public SignInPlace getPlace(String token) {
            return new SignInPlace(token);
        }
    }
}
