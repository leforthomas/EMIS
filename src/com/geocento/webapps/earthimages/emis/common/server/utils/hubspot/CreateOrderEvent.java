package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateOrderEvent extends CreateEvent {

    @JsonProperty("eventId")
    public String orderId;
    @JsonProperty("timestamp")
    public Long timestamp;

    public CreateOrderEvent() {
        eventTypeId = "279490";
    }

}
