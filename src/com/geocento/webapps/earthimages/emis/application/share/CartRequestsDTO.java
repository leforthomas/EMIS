package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.List;

public class CartRequestsDTO implements Serializable {

    List<ProductRequestDTO> productRequestDTOs;
    List<ProductOrderDTO> productOrders;

    public CartRequestsDTO() {
    }

    public List<ProductRequestDTO> getProductRequestDTOs() {
        return productRequestDTOs;
    }

    public void setProductRequestDTOs(List<ProductRequestDTO> productRequestDTOs) {
        this.productRequestDTOs = productRequestDTOs;
    }

    public List<ProductOrderDTO> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrderDTO> productOrders) {
        this.productOrders = productOrders;
    }
}
