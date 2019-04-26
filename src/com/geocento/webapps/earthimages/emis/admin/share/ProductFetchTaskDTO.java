package com.geocento.webapps.earthimages.emis.admin.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.STATUS;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by thomas on 14/10/2016.
 */
public class ProductFetchTaskDTO implements Serializable {

    Long id;
    STATUS status;
    String statusMessage;
    Date created;
    Date completed;
    Date createdDate;
    Date fetchDate;
    // download task part
    Long downloadTaskId;
    // publish task part
    Long publishTaskId;

    String productOrderId;
    private String orderId;

    public ProductFetchTaskDTO() {
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public String getProductOrderId() {
        return productOrderId;
    }

    public void setProductOrderId(String productOrderId) {
        this.productOrderId = productOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
