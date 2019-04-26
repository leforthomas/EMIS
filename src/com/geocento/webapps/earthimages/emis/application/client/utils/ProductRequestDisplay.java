package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.application.share.ProductRequestDTO;

import java.util.List;

/**
 * Created by thomas on 27/01/2017.
 */
public class ProductRequestDisplay {

    ProductRequestDTO productRequestDTO;
    List<ProductOrderDTO> productOrderDTOs;

    boolean visible;
    boolean ordersVisible;

    public ProductRequestDisplay() {
    }

    public ProductRequestDTO getProductRequestDTO() {
        return productRequestDTO;
    }

    public void setProductRequestDTO(ProductRequestDTO productRequestDTO) {
        this.productRequestDTO = productRequestDTO;
    }

    public List<ProductOrderDTO> getProductOrderDTOs() {
        return productOrderDTOs;
    }

    public void setProductOrderDTOs(List<ProductOrderDTO> productOrderDTOs) {
        this.productOrderDTOs = productOrderDTOs;
    }

    public boolean isOrdersVisible() {
        return ordersVisible;
    }

    public void setOrdersVisible(boolean ordersVisible) {
        this.ordersVisible = ordersVisible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
