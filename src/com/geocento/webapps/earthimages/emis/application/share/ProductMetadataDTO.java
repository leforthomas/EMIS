package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.productpublisher.persistence.PRODUCT_TYPE;

import java.io.Serializable;

/**
 * Created by thomas on 23/09/2017.
 */
public class ProductMetadataDTO implements Serializable{

    private String name;
    private String publishUri;
    private Long publishRequestId;
    private PRODUCT_TYPE productType;
    private String coordinatesWKT;
    private String description;

    public ProductMetadataDTO() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPublishUri(String publishUri) {
        this.publishUri = publishUri;
    }

    public String getPublishUri() {
        return publishUri;
    }

    public void setPublishRequestId(Long publishRequestId) {
        this.publishRequestId = publishRequestId;
    }

    public Long getPublishRequestId() {
        return publishRequestId;
    }

    public void setProductType(PRODUCT_TYPE productType) {
        this.productType = productType;
    }

    public PRODUCT_TYPE getProductType() {
        return productType;
    }

    public void setCoordinatesWKT(String coordinatesWKT) {
        this.coordinatesWKT = coordinatesWKT;
    }

    public String getCoordinatesWKT() {
        return coordinatesWKT;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
