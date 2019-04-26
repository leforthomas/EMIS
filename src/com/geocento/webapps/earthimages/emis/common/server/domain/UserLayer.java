package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.LAYER_TYPE;

import javax.persistence.*;
import java.util.Date;


@Entity
public class UserLayer {

	@Id
    @GeneratedValue
	Long id;

    LAYER_TYPE layerType;

    String name;
    @Column(length = 1000)
    String description;
    @Column(length = 1000)
    String baseUrl;
    String layerName;
    String version;

    @Column(length = 1000)
    String wcsUrl;
    String coverageId;

    @ManyToOne
    UserLayers userLayers;

    @Temporal(TemporalType.TIMESTAMP)
    Date creationTime;

    public UserLayer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LAYER_TYPE getLayerType() {
        return layerType;
    }

    public void setLayerType(LAYER_TYPE layerType) {
        this.layerType = layerType;
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

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWcsUrl() {
        return wcsUrl;
    }

    public void setWcsUrl(String wcsUrl) {
        this.wcsUrl = wcsUrl;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public UserLayers getUserLayers()
    {
        return userLayers;
    }

    public void setUserLayers(UserLayers userLayer)
    {
        this.userLayers = userLayer;
    }
}
