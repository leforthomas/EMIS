package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EIProSampleRequestEvent extends CreateEvent {

    @JsonProperty("origin")
    public String origin;

    @JsonProperty("samplesList")
    public String samplesList;

    public EIProSampleRequestEvent() {
        eventTypeId = "279604";
    }

}
