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
    private ORDER_STATUS status;
    private boolean showInfo;
    private String thumbnail;
    private Date creationDate;
    private String description;
    private EOBounds bounds;
    private int numOfProducts;
    private List<AOI> aois;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ORDER_STATUS getStatus() {
        return status;
    }

    public void setStatus(ORDER_STATUS status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EOBounds getBounds()
    {
        return bounds;
    }

    public void setBounds(EOBounds bounds)
    {
        this.bounds = bounds;
    }

    public int getNumOfProducts()
    {
        return numOfProducts;
    }

    public void setNumOfProducts(int numOfProducts)
    {
        this.numOfProducts = numOfProducts;
    }

    public List<AOI> getAois()
    {
        return aois;
    }

    public void setAois(List<AOI> aois)
    {
        this.aois = aois;
    }
}
