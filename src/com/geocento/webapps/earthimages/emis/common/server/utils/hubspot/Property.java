package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {

    String value;

    // ignore the rest

    public Property() {
    }

    public Property(String key) {

    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
