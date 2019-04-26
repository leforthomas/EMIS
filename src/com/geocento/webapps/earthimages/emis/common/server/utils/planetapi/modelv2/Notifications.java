package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Notifications {
    @JsonProperty("webhook")
    public Webhook webhook;
}

