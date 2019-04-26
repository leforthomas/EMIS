package com.geocento.webapps.earthimages.emis.application.server.imageapi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.metaaps.webapps.earthimages.extapi.server.domain.orders.PRODUCTORDER_STATUS;

import java.util.Date;

/**
 * Created by thomas on 16/10/2017.
 */
public class ProductOrderStatus {

    String orderId;
    Long productOrderId;
    PRODUCTORDER_STATUS status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    Date deliveryTime;
    String downloadUrl;

    public ProductOrderStatus() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getProductOrderId() {
        return productOrderId;
    }

    public void setProductOrderId(Long productOrderId) {
        this.productOrderId = productOrderId;
    }

    public PRODUCTORDER_STATUS getStatus() {
        return status;
    }

    public void setStatus(PRODUCTORDER_STATUS status) {
        this.status = status;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
