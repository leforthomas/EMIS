package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EIProRegistrationEvent extends CreateEvent {

    @JsonProperty("userName")
    public String userName;

    public EIProRegistrationEvent() {
        eventTypeId = "279493";
    }

}
