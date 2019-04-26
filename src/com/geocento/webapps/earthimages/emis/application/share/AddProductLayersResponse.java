package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class AddProductLayersResponse implements Serializable {

    HashMap<String, List<ProductLayerDTO>> layersAdded;

    public AddProductLayersResponse() {
    }

    public HashMap<String, List<ProductLayerDTO>> getLayersAdded() {
        return layersAdded;
    }

    public void setLayersAdded(HashMap<String, List<ProductLayerDTO>> layersAdded) {
        this.layersAdded = layersAdded;
    }
}
