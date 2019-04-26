package com.geocento.webapps.earthimages.emis.common.share;

import com.metaaps.webapps.libraries.client.map.EOBounds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LayerDTO implements LayerResource {

	LAYER_TYPE layerType;

	String name;
	String description;
	String baseUrl;
	String layerName;
	String version;
	EOBounds bounds;
    String credits;

	// is layer a possible base layer for a map
	boolean isBaseLayer;

	// layer styles supported by the server
	ArrayList<LayerStyle> styles = new ArrayList<LayerStyle>();

	// SRS to use for the call
	String supportedSRS;
    private boolean timeEnabled;
    private List<Date> dates;

    // matching coverage if any
    CoverageDTO coverageDTO;

	public LayerDTO() {
	}

    public LAYER_TYPE getLayerType() {
        return layerType;
    }

    public void setLayerType(LAYER_TYPE layerType) {
        this.layerType = layerType;
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

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public EOBounds getBounds() {
        return bounds;
    }

    public void setBounds(EOBounds bounds) {
        this.bounds = bounds;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public boolean isBaseLayer() {
        return isBaseLayer;
    }

    public void setBaseLayer(boolean baseLayer) {
        isBaseLayer = baseLayer;
    }

    public ArrayList<LayerStyle> getStyles() {
        return styles;
    }

    public void setStyles(ArrayList<LayerStyle> styles) {
        this.styles = styles;
    }

    public String getSupportedSRS() {
        return supportedSRS;
    }

    public void setSupportedSRS(String supportedSRS) {
        this.supportedSRS = supportedSRS;
    }

    public boolean isTimeEnabled() {
        return timeEnabled;
    }

    public void setTimeEnabled(boolean timeEnabled) {
        this.timeEnabled = timeEnabled;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    public CoverageDTO getCoverageDTO() {
        return coverageDTO;
    }

    public void setCoverageDTO(CoverageDTO coverageDTO) {
        this.coverageDTO = coverageDTO;
    }
}
