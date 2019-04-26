package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.List;

public class SamplesResult implements Serializable {

    List<SampleDTO> samples;
    int totalResults;

    public SamplesResult() {
    }

    public List<SampleDTO> getSamples() {
        return samples;
    }

    public void setSamples(List<SampleDTO> samples) {
        this.samples = samples;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
