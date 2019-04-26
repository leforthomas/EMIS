package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * Created by thomas on 9/01/15.
 */
@Entity
public class UserSettings {

    @Id
    @GeneratedValue
    Long id;

    @OneToOne
    User owner;

    double productsOpacity;
    double overlaysOpacity;
    double aoiOpacity;

    public UserSettings() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public double getProductsOpacity() {
        return productsOpacity;
    }

    public void setProductsOpacity(double productsOpacity) {
        this.productsOpacity = productsOpacity;
    }

    public double getOverlaysOpacity() {
        return overlaysOpacity;
    }

    public void setOverlaysOpacity(double overlaysOpacity) {
        this.overlaysOpacity = overlaysOpacity;
    }

    public void setAoiOpacity(double aoIOpacity) {
        this.aoiOpacity = aoIOpacity;
    }

    public double getAoiOpacity() {
        return aoiOpacity;
    }
}
