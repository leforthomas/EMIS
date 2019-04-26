package com.geocento.webapps.earthimages.emis.application.server.utils;

import com.metaaps.webapps.earthimages.extapi.server.domain.ProductFilters;
import com.metaaps.webapps.earthimages.extapi.server.domain.SensorFilters;

/**
 * Created by thomas on 04/03/2016.
 */
public class SelectionRule {

    String sensorsRegexp;
    SensorFilters sensorFilters;
    ProductFilters productFilters;

    public SelectionRule() {
    }

    public String getSensorsRegexp() {
        return sensorsRegexp;
    }

    public void setSensorsRegexp(String sensorsRegexp) {
        this.sensorsRegexp = sensorsRegexp;
    }

    public SensorFilters getSensorFilters() {
        return sensorFilters;
    }

    public void setSensorFilters(SensorFilters sensorFilters) {
        this.sensorFilters = sensorFilters;
    }

    public ProductFilters getProductFilters() {
        return productFilters;
    }

    public void setProductFilters(ProductFilters productFilters) {
        this.productFilters = productFilters;
    }
}
