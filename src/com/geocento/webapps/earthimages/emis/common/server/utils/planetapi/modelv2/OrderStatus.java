package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatus {

    @JsonProperty("_links")
    public Links links;
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
    @JsonProperty("error_hints")
    public List<String> errorHints;
    @JsonProperty("products")
    public List<Products> products;
    @JsonProperty("state")
    public String state;

    public OrderStatus() {
    }

    public static class Results {
        @JsonProperty("delivery")
        public String delivery;
        @JsonProperty("name")
        public String name;
        @JsonProperty("expires_at")
        public String expiresAt;
        @JsonProperty("location")
        public String location;
    }

    public static class Links {
        @JsonProperty("_self")
        public String self;
        @JsonProperty("results")
        public List<Results> results;
    }

}
