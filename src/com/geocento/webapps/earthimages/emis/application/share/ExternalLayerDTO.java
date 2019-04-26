package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.productpublisher.persistence.PRODUCT_TYPE;

import java.io.Serializable;

public class ExternalLayerDTO implements Serializable {

    String id;
    String name;
    String description;
    PRODUCT_TYPE productType;
    String publishUri;
    String bounds;
    boolean displayed;
    String sldName;

    public ExternalLayerDTO() {
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

    public PRODUCT_TYPE getProductType() {
        return productType;
    }

    public void setProductType(PRODUCT_TYPE productType) {
        this.productType = productType;
    }

    public String getPublishUri() {
        return publishUri;
    }

    public void setPublishUri(String publishUri) {
        this.publishUri = publishUri;
    }

    public String getBounds() {
        return bounds;
    }

    public void setBounds(String bounds) {
        this.bounds = bounds;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public String getSldName() {
        return sldName;
    }

    public void setSldName(String sldName) {
        this.sldName = sldName;
    }
}
