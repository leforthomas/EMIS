package com.geocento.webapps.earthimages.emis.application.share;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
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
    List<ProductOrderDTO> productOrders;

    public EventDTO() {
    }

    public List<ProductOrderDTO> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrderDTO> productOrders) {
        this.productOrders = productOrders;
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
