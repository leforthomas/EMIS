package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.entities.PUBLICATION_STATUS;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by thomas on 14/10/2016.
 */
@Entity
public class ProductPublishRequest {

    @Id
    @GeneratedValue
    Long id;

    // publish task part
    Long publishTaskId;

    @Column(length = 100)
    String name;
    @Column(length = 1000)
    String description;
    @Column(length = 1000)
    String thumbnailURL;

    @ManyToOne
    ProductOrder productOrder;

    // task related data
    @Enumerated(EnumType.STRING)
    PUBLICATION_STATUS status;
    @Column(length = 10000)
    String statusMessage;
    @Temporal(TemporalType.TIMESTAMP)
    Date created;
    @Temporal(TemporalType.TIMESTAMP)
    Date completed;

    public ProductPublishRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPublishTaskId() {
        return publishTaskId;
    }

    public void setPublishTaskId(Long publishTaskId) {
        this.publishTaskId = publishTaskId;
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

    public ProductOrder getProductOrder() {
        return productOrder;
    }

    public void setProductOrder(ProductOrder productOrder) {
        this.productOrder = productOrder;
    }

    public PUBLICATION_STATUS getStatus() {
        return status;
    }

    public void setStatus(PUBLICATION_STATUS status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
