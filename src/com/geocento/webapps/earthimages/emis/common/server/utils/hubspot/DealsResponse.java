package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DealsResponse {

    @JsonProperty("deals")
    public List<DealContent> deals;
    @JsonProperty("hasMore")
    public boolean hasMore;
    @JsonProperty("offset")
    public int offset;

    public DealsResponse() {
    }

}
