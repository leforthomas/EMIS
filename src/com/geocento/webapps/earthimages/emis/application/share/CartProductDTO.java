package com.geocento.webapps.earthimages.emis.application.share;

import com.metaaps.webapps.earthimages.extapi.server.domain.TYPE;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by thomas on 12/01/15.
 *
 * DTO for cart products to host the product request values
 *
 */
public class CartProductDTO implements Serializable {

    private Long productRequestId;
    private String productId;
    private TYPE type;
    private String satelliteName;
    private String sensorName;
    private Date start;
    private String coordinatesWKT;

    public CartProductDTO() {
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Long getProductRequestId() {
        return productRequestId;
    }

    public void setProductRequestId(Long productRequestId) {
        this.productRequestId = productRequestId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setCoordinatesWKT(String coordinatesWKT) {
        this.coordinatesWKT = coordinatesWKT;
    }

    public String getCoordinatesWKT() {
        return coordinatesWKT;
    }
}
