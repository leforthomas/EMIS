package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequest {

    @JsonProperty("name")
    public String name;
    @JsonProperty("subscription_id")
    public Integer subscriptionId;
    @JsonProperty("products")
    public List<Products> products;
    @JsonProperty("tools")
    public List<Tools> tools;
    @JsonProperty("delivery")
    public Delivery delivery;
    @JsonProperty("notifications")
    public Notifications notifications;

    public OrderRequest() {
    }
}
