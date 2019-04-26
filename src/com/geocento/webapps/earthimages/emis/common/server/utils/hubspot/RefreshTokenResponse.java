package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshTokenResponse {

    @JsonProperty("refresh_token")
    public String refresh_token;
    @JsonProperty("access_token")
    public String access_token;
    @JsonProperty("expires_in")
    public int expires_in;

    @JsonProperty("status")
    public String status;
    @JsonProperty("message")
    public String message;
    @JsonProperty("correlationId")
    public String correlationId;
    @JsonProperty("requestId")
    public String requestId;

}
