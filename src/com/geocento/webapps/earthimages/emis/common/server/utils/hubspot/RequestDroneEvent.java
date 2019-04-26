package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestDroneEvent extends CreateEvent {

    @JsonProperty("queryId")
    public String queryId;

    public RequestDroneEvent() {
        eventTypeId = "279492";
    }

}
