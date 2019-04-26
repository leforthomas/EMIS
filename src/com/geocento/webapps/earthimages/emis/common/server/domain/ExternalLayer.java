package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.productpublisher.persistence.PRODUCT_TYPE;

import javax.persistence.*;

@Entity
public class ExternalLayer {

    @Id
    String id;

    @Column(length = 100)
    String name;

    @Column(length = 1000)
    String description;

    @Enumerated
    PRODUCT_TYPE productType;

    @ManyToOne
    PublishedLayer publishedLayer;

    @Column(length = 1000)
    String publishUri;

    @Column(length = 1000)
    String bounds;

    boolean displayed;

    @Column(length = 100)
    String sldName;

    public ExternalLayer() {
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
