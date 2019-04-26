package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {

    @JsonProperty("created_on")
    public String createdOn;
    @JsonProperty("id")
    public String id;
    @JsonProperty("last_message")
    public String lastMessage;
    @JsonProperty("last_modified")
    public String lastModified;
    @JsonProperty("name")
    public String name;
    @JsonProperty("products")
    public List<Products> products;
    @JsonProperty("state")
    public String state;

}
