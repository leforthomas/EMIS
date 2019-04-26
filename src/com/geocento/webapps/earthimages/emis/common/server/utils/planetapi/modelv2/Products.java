package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Products {

    @JsonProperty("item_ids")
    public List<String> itemIds;
    @JsonProperty("item_type")
    public String itemType;
    @JsonProperty("product_bundle")
    public String productBundle;

    public Products() {
    }

}

