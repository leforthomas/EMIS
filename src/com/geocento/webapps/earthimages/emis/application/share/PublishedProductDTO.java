package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.PUBLICATION_STATUS;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PublishedProductDTO implements Serializable {

    Long id;
    String name;
    String description;
    PUBLICATION_STATUS status;
    String statusMessage;
    List<ProductMetadataDTO> products;
    Date processedDate;
    private String thumbnailURL;
    private boolean visible;

    public PublishedProductDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public List<ProductMetadataDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductMetadataDTO> products) {
        this.products = products;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
