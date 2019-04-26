package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;

public class AISettingsPlace extends AIPlace {

    public static enum TOKENS {};

	private Place place;

    public AISettingsPlace() {
    }

    public AISettingsPlace(String token) {
        this.token = token;
    }

    public AISettingsPlace(String token, Place place) {
        this.token = token;
        this.place = place;
    }

    public Place getPlace() {
		return place;
	}

    @Prefix("settings")
	public static class Tokenizer implements PlaceTokenizer<AISettingsPlace> {
        @Override
        public String getToken(AISettingsPlace place) {
            return place.getToken();
        }

        @Override
        public AISettingsPlace getPlace(String token) {
            return new AISettingsPlace(token);
        }
    }
}
