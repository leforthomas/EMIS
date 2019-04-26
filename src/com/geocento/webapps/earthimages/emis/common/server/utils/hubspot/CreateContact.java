package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import java.util.List;

public class CreateContact {

    private List<ContactProperty> properties;

    public CreateContact() {
    }

    public List<ContactProperty> getProperties() {
        return this.properties;
    }

    public void setProperties(List<ContactProperty> properties) {
        this.properties = properties;
    }
}
