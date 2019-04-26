package com.geocento.webapps.earthimages.emis.application.client.place;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({
        EventsPlace.Tokenizer.class,
        ViewEventPlace.Tokenizer.class,
        SignInPlace.Tokenizer.class
})
public interface AppPlaceHistoryMapper extends PlaceHistoryMapper
{
}
