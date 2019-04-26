package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateEvent {

    @JsonProperty("id")
    public String id;
    @JsonProperty("eventTypeId")
    public String eventTypeId;
    @JsonProperty("email")
    public String email;
    @JsonProperty("vid")
    public String vid;

}
