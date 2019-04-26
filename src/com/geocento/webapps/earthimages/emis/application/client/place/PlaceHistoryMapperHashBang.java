package com.geocento.webapps.earthimages.emis.application.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

public class PlaceHistoryMapperHashBang implements PlaceHistoryMapper {

    private PlaceHistoryMapper historyMapper;

    public PlaceHistoryMapperHashBang(PlaceHistoryMapper historyMapper) {
        this.historyMapper = historyMapper;
    }

    @Override
    public Place getPlace(String token) {
        Place p=historyMapper.getPlace(token.startsWith("!") ? token.substring(1) : token);
        if(p!=null) return p;
        return historyMapper.getPlace(token);
    }

    @Override
    public String getToken(Place place) {
        return historyMapper.getToken(place);
    }
}