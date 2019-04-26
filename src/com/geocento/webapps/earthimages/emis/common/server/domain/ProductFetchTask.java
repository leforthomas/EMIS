package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.entities.STATUS;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by thomas on 14/10/2016.
 */
@Entity
public class ProductFetchTask {

    @Id
    @GeneratedValue
    Long id;

    // task related data
    @Enumerated(EnumType.STRING)
    STATUS status;
    @Column(length = 10000)
    String statusMessage;
    @Temporal(TemporalType.TIMESTAMP)
    Date created;
    @Temporal(TemporalType.TIMESTAMP)
    Date completed;
    @Temporal(TemporalType.TIMESTAMP)
    Date fetchDate;

    // download task part
    // either locally fetched or from EarthImages
    Long downloadTaskId;
    String EIOrderId;
    Long EIProductOrderId;

    // publish task part
    Long publishTaskId;

    @OneToOne
    ProductOrder productOrder;

    @Column(length = 1000)
    String planetClipUrl;
    @Column(length = 1000)
    String planetOrderId;

    int attempt;

    public ProductFetchTask() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
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

    public Date getFetchDate() {
        return fetchDate;
    }

    public void setFetchDate(Date fetchDate) {
        this.fetchDate = fetchDate;
    }

    public Long getDownloadTaskId() {
        return downloadTaskId;
    }

    public void setDownloadTaskId(Long downloadTaskId) {
        this.downloadTaskId = downloadTaskId;
    }

    public Long getPublishTaskId() {
        return publishTaskId;
    }

    public void setPublishTaskId(Long publishTaskId) {
        this.publishTaskId = publishTaskId;
    }

    public ProductOrder getProductOrder() {
        return productOrder;
    }

    public void setProductOrder(ProductOrder productOrder) {
        this.productOrder = productOrder;
    }

    public Long getEIProductOrderId() {
        return EIProductOrderId;
    }

    public void setEIProductOrderId(Long EIProductOrderId) {
        this.EIProductOrderId = EIProductOrderId;
    }

    public String getEIOrderId() {
        return EIOrderId;
    }

    public void setEIOrderId(String EIOrderId) {
        this.EIOrderId = EIOrderId;
    }

    public void setPlanetClipUrl(String planetClipUrl) {
        this.planetClipUrl = planetClipUrl;
    }

    public String getPlanetClipUrl() {
        return planetClipUrl;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public void setPlanetOrderId(String planetOrderId) {
        this.planetOrderId = planetOrderId;
    }

    public String getPlanetOrderId() {
        return planetOrderId;
    }
}
