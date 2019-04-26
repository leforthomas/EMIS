package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.metaaps.webapps.libraries.client.map.EOBounds;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 04/05/2017.
 */
public class WorkspaceSummaryDTO implements Serializable {

    private String id;
    private String name;
    private String description;
    private Date creationDate;
    private EOBounds bounds;
    private int numOfProducts;
    private int publishedProducts;
    private List<AOI> layerFootprints;

    public WorkspaceSummaryDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public EOBounds getBounds() {
        return bounds;
    }

    public void setBounds(EOBounds bounds) {
        this.bounds = bounds;
    }

    public int getNumOfProducts() {
        return numOfProducts;
    }

    public void setNumOfProducts(int numOfProducts) {
        this.numOfProducts = numOfProducts;
    }

    public int getPublishedProducts() {
        return publishedProducts;
    }

    public void setPublishedProducts(int publishedProducts) {
        this.publishedProducts = publishedProducts;
    }

    public List<AOI> getLayerFootprints() {
        return layerFootprints;
    }

    public void setLayerFootprints(List<AOI> layerFootprints) {
        this.layerFootprints = layerFootprints;
    }
}
