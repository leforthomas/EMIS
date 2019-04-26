package com.geocento.webapps.earthimages.emis.common.share;

import java.io.Serializable;

public class CoverageDTO implements Serializable {

    String baseUrl;
    String coverageId;
    private String version;

    public CoverageDTO() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCoverageId() {
        return coverageId;
    }

    public void setCoverageId(String coverageId) {
        this.coverageId = coverageId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
