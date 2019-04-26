package com.geocento.webapps.earthimages.emis.admin.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by thomas on 14/10/2016.
 */
public class ProductOrderDTO implements Serializable {

    String id;
    String orderId;
    PRODUCTORDER_STATUS status;
    String title;
    String description;
    Date created;
    private String geocentoId;
    private Date estimatedDeliveryTime;
    private Long EIProductOrderId;

    public ProductOrderDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public PRODUCTORDER_STATUS getStatus() {
        return status;
    }

    public void setStatus(PRODUCTORDER_STATUS status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getGeocentoId() {
        return geocentoId;
    }

    public void setGeocentoId(String geocentoId) {
        this.geocentoId = geocentoId;
    }

    public Date getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(Date estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public Long getEIProductOrderId() {
        return EIProductOrderId;
    }

    public void setEIProductOrderId(Long EIProductOrderId) {
        this.EIProductOrderId = EIProductOrderId;
    }
}
