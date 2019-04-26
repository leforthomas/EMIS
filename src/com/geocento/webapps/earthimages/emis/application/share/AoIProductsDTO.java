package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.Satellite;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.ProductPolicy;

import java.io.Serializable;
import java.util.List;

/**
 * Created by thomas on 22/10/2015.
 */
public class AoIProductsDTO implements Serializable {

    private AOI aoi;
    private List<Product> products;
    private List<Satellite> satellites;
    private List<ProductPolicy> policies;

    public AoIProductsDTO() {
    }

    public AOI getAoi() {
        return aoi;
    }

    public void setAoi(AOI aoi) {
        this.aoi = aoi;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Satellite> getSatellites() {
        return satellites;
    }

    public void setSatellites(List<Satellite> satellites) {
        this.satellites = satellites;
    }

    public List<ProductPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<ProductPolicy> policies) {
        this.policies = policies;
    }
}
