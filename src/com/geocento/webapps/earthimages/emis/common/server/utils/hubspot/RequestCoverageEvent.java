package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestCoverageEvent extends CreateEvent {

    @JsonProperty("queryId")
    public String queryId;

    public RequestCoverageEvent() {
        eventTypeId = "279491";
    }

}
