package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

public class DealProperty {

    String name;
    String value;

    public DealProperty() {
    }

    public DealProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
