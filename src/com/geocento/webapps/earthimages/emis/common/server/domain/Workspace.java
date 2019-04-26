package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Workspace {

    @Id
    String id;

    @ManyToOne
    User owner;

    @Column(length = 100)
    String name;

    @Column(length = 1000)
    String description;

    // ordered list of products
    @ManyToMany
    List<ProductOrder> productOrders;

    // list of generated products from product orders
    @OneToMany
    List<ProductPublishRequest> publishProductRequests;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "workspace", cascade = CascadeType.ALL)
    List<PublishedLayer> publishedLayers;

    @Temporal(TemporalType.TIMESTAMP)
    Date creationDate;

    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified;

    public Workspace() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
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

    public List<ProductOrder> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrder> productOrders) {
        this.productOrders = productOrders;
    }

    public List<ProductPublishRequest> getPublishProductRequests() {
        return publishProductRequests;
    }

    public void setPublishProductRequests(List<ProductPublishRequest> publishProductRequests) {
        this.publishProductRequests = publishProductRequests;
    }

    public List<PublishedLayer> getPublishedLayers() {
        return publishedLayers;
    }

    public void setPublishedLayers(List<PublishedLayer> publishedLayers) {
        this.publishedLayers = publishedLayers;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
