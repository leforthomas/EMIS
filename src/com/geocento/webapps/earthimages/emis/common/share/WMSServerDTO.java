package com.geocento.webapps.earthimages.emis.common.share;

import java.util.List;

public class WMSServerDTO implements LayerResource {

    String id;
    String name;
    String description;

    String baseUrl;

    List<LayerDTO> layers;

    boolean hasWCS;
    private String WCSUrl;

    public WMSServerDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LayerDTO> getLayers() {
        return layers;
    }

    public void setLayers(List<LayerDTO> layers) {
        this.layers = layers;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setWCSUrl(String WCSUrl) {
        this.WCSUrl = WCSUrl;
    }

    public String getWCSUrl() {
        return WCSUrl;
    }
}
