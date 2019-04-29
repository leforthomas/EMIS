package com.geocento.webapps.earthimages.emis.application.share;

import com.metaaps.webapps.earthimages.extapi.server.domain.Product;

import java.io.Serializable;
import java.util.List;

/**
 * Created by thomas on 04/05/2017.
 */
public class EventDTO implements Serializable {

    String id;

    // from the events api
    EventDescription eventDescription;

    // from the EarthImages API
    List<Product> productCandidates;

    // collected product orders
    List<ProductOrderDTO> productsOrdered;

    public EventDTO() {
    }

    public List<ProductOrderDTO> getProductsOrdered() {
        return productsOrdered;
    }

    public void setProductsOrdered(List<ProductOrderDTO> productsOrdered) {
        this.productsOrdered = productsOrdered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventDescription getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(EventDescription eventDescription) {
        this.eventDescription = eventDescription;
    }

    public List<Product> getProductCandidates() {
        return productCandidates;
    }

    public void setProductCandidates(List<Product> productCandidates) {
        this.productCandidates = productCandidates;
    }
}
