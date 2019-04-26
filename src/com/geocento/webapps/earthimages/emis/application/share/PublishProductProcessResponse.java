package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.List;

public class PublishProductProcessResponse implements Serializable {

    String message;
    List<PublishedProductDTO> publishedProducts;

    public PublishProductProcessResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<PublishedProductDTO> getPublishedProducts() {
        return publishedProducts;
    }

    public void setPublishedProducts(List<PublishedProductDTO> publishedProducts) {
        this.publishedProducts = publishedProducts;
    }
}
