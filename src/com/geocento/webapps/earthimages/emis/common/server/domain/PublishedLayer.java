package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.List;

@Entity
public class PublishedLayer {

    @Id
    String id;

    @Column(length = 100)
    String name;

    @Column(length = 1000)
    String description;

    @Column(length = 1000)
    String thumbnailUrl;

    @ManyToOne
    Workspace workspace;

    @OneToMany(mappedBy = "publishedLayer", cascade = CascadeType.ALL)
    List<ProductLayer> productLayers;

    @OneToMany(mappedBy = "publishedLayer", cascade = CascadeType.ALL)
    List<ExternalLayer> additionalLayers;

    // publishing part
    @Column(length = 100)
    String publishedId;

    @Column(length = 100)
    String publishedWorkspace;

    @Column(length = 100)
    boolean published;

    public PublishedLayer() {
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

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public List<ProductLayer> getProductLayers() {
        return productLayers;
    }

    public void setProductLayers(List<ProductLayer> layerProducts) {
        this.productLayers = layerProducts;
    }

    public List<ExternalLayer> getAdditionalLayers() {
        return additionalLayers;
    }

    public void setAdditionalLayers(List<ExternalLayer> additionalLayers) {
        this.additionalLayers = additionalLayers;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getPublishedId() {
        return publishedId;
    }

    public void setPublishedId(String publishedId) {
        this.publishedId = publishedId;
    }

    public void setPublishedWorkspace(String publishedWorkspace) {
        this.publishedWorkspace = publishedWorkspace;
    }

    public String getPublishedWorkspace() {
        return publishedWorkspace;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isPublished() {
        return published;
    }
}
