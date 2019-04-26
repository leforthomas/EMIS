package com.geocento.webapps.earthimages.emis.admin.share;

import java.io.Serializable;

/**
 * Created by thomas on 04/05/2017.
 */
public class SampleDTO implements Serializable {

    String id;
    String name;
    String description;
    String keywords;
    String thumbnail;
    ProductOrderDTO productOrderDTO;

    public SampleDTO() {
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

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public ProductOrderDTO getProductOrderDTO() {
        return productOrderDTO;
    }

    public void setProductOrderDTO(ProductOrderDTO productOrderDTO) {
        this.productOrderDTO = productOrderDTO;
    }
}
