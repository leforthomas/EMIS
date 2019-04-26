package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

public class ContactProperty {

    String property;
    String value;

    public ContactProperty() {
    }

    public ContactProperty(String property, String value) {
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return this.property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
