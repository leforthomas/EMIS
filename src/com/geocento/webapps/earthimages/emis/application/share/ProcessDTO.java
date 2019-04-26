package com.geocento.webapps.earthimages.emis.application.share;

import com.metaaps.webapps.libraries.client.property.domain.Property;

import java.io.Serializable;
import java.util.List;

public class ProcessDTO implements Serializable {

    private Long id;
    private String name;
    private String description;
    private String supportedPlatformProducts;
    private List<Property> parameters;

    public ProcessDTO() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setSupportedPlatformProducts(String supportedPlatformProducts) {
        this.supportedPlatformProducts = supportedPlatformProducts;
    }

    public String getSupportedPlatformProducts() {
        return supportedPlatformProducts;
    }

    public List<Property> getParameters() {
        return parameters;
    }

    public void setParameters(List<Property> parameters) {
        this.parameters = parameters;
    }
}
