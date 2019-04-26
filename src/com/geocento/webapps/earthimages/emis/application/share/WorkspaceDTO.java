package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 04/05/2017.
 */
public class WorkspaceDTO implements Serializable {

    String id;
    String name;
    String description;
    List<ProductOrderDTO> productOrders;
    List<PublishedProductDTO> publishedProducts;
    Date creationDate;
    private List<PublishedLayerDTO> publishedLayers;

    public WorkspaceDTO() {
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

    public List<ProductOrderDTO> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrderDTO> productOrders) {
        this.productOrders = productOrders;
    }

    public List<PublishedProductDTO> getPublishedProducts() {
        return publishedProducts;
    }

    public void setPublishedProducts(List<PublishedProductDTO> publishedProducts) {
        this.publishedProducts = publishedProducts;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<PublishedLayerDTO> getPublishedLayers() {
        return publishedLayers;
    }

    public void setPublishedLayers(List<PublishedLayerDTO> publishedLayers) {
        this.publishedLayers = publishedLayers;
    }
}
