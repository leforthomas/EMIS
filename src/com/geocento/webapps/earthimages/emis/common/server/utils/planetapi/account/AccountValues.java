
package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountValues {

    @JsonProperty("quota_sqkm")
    private Long quotaSqkm;
    @JsonProperty("quota_style")
    private String quotaStyle;
    @JsonProperty("quota_used")
    private Long quotaUsed;
    private String reference;
    private String state;
    @JsonProperty("updated_at")
    private String updatedAt;

    public Long getQuotaSqkm() {
        return quotaSqkm;
    }

    public String getQuotaStyle() {
        return quotaStyle;
    }

    public Long getQuotaUsed() {
        return quotaUsed;
    }

    public String getReference() {
        return reference;
    }

    public String getState() {
        return state;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

}
