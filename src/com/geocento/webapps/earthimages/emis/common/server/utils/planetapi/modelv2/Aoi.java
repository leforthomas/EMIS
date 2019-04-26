package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Aoi {
    @JsonProperty("type")
    public String type;
    @JsonProperty("coordinates")
    public double[][][] coordinates;
}

