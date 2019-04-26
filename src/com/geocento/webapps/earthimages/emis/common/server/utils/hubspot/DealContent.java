package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DealContent {

    public static class Associations {
        @JsonProperty("associatedVids")
        public int[] associatedVids;
        @JsonProperty("associatedCompanyIds")
        public int[] associatedCompanyIds;
        @JsonProperty("associatedDealIds")
        public int[] associatedDealIds;
    }

    @JsonProperty("portalId")
    public int portalId;
    @JsonProperty("dealId")
    public int dealId;
    @JsonProperty("isDeleted")
    public boolean isDeleted;
    @JsonProperty("associations")
    public Associations associations;
    @JsonProperty("properties")
    private Map<String, Property> properties;

    public DealContent() {
    }

}
