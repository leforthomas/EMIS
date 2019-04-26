package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.List;

public class PublishedLayerDTO implements Serializable {

    String id;
    String name;
    String description;
    List<ProductLayerDTO> productLayers;
    List<ExternalLayerDTO> additionalLayers;
    private boolean visible;
    private String thumbnailUrl;
    private String publishedWorkspace;
    private String publishedLayer;
    private boolean published;

    public PublishedLayerDTO() {
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

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setProductLayers(List<ProductLayerDTO> productLayers) {
        this.productLayers = productLayers;
    }

    public List<ProductLayerDTO> getProductLayers() {
        return productLayers;
    }

    public List<ExternalLayerDTO> getAdditionalLayers() {
        return additionalLayers;
    }

    public void setAdditionalLayers(List<ExternalLayerDTO> additionalLayers) {
        this.additionalLayers = additionalLayers;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPublishedWorkspace(String publishedWorkspace) {
        this.publishedWorkspace = publishedWorkspace;
    }

    public String getPublishedWorkspace() {
        return publishedWorkspace;
    }

    public void setPublishedLayer(String publishedLayer) {
        this.publishedLayer = publishedLayer;
    }

    public String getPublishedLayer() {
        return publishedLayer;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isPublished() {
        return published;
    }
}
