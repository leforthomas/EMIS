package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Delivery {

    @JsonProperty("archive_type")
    public String archiveType;
    @JsonProperty("archive_filename")
    public String archiveFilename;

}
