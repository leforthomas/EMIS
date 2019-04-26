package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import java.util.Map;

public class ContactsResponse {

    private Long vid;
    private Map<String, Property> properties;

    public ContactsResponse() {
    }

    public Long getVid() {
        return this.vid;
    }

    public void setVid(Long vid) {
        this.vid = vid;
    }

    public Map<String, Property> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }
}
