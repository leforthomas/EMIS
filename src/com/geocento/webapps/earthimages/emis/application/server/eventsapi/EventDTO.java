
package com.geocento.webapps.earthimages.emis.application.server.eventsapi;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.Expose;
import org.geojson.geometry.Geometry;
import org.geojson.util.FeaturePropertiesDeserializer;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDTO {

    final String type = "Feature";
    Geometry geometry;
    Properties properties;

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
