package com.geocento.webapps.earthimages.emis.admin.client.place;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;

@WithTokenizers({AISignInPlace.Tokenizer.class,
        AISettingsPlace.Tokenizer.class,
        AIPublishPlace.Tokenizer.class,
        AIOrdersPlace.Tokenizer.class,
        AIUsersPlace.Tokenizer.class,
        AISamplesPlace.Tokenizer.class,
        AILogsPlace.Tokenizer.class})
public interface AppPlaceHistoryMapper extends PlaceHistoryMapper
{
}
