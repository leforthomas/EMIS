package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreateDealRequest {

    public static class Associations {
        @JsonProperty("associatedCompanyIds")
        public List<Long> associatedCompanyIds;
        @JsonProperty("associatedVids")
        public int[] associatedVids;
    }

    @JsonProperty("associations")
    public Associations associations;
    @JsonProperty("properties")
    public List<DealProperty> properties;

    public CreateDealRequest() {
    }

}
