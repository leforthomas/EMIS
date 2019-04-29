package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.metaaps.webapps.libraries.client.map.EOBounds;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 04/05/2017.
 */
public class EventSummaryDTO implements Serializable {

    private String id;
    private String name;
    private AOI aoi;
    private String description;
    private String thumbnail;
    private Date startDate;
    private Date endDate;
    private Date creationDate;
    private int numOfProducts;

    private boolean showInfo;

    public EventSummaryDTO() {
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

    public AOI getAoi() {
        return aoi;
    }

    public void setAoi(AOI aoi) {
        this.aoi = aoi;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getNumOfProducts() {
        return numOfProducts;
    }

    public void setNumOfProducts(int numOfProducts) {
        this.numOfProducts = numOfProducts;
    }

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }
}
