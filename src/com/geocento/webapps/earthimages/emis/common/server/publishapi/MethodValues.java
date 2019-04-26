package com.geocento.webapps.earthimages.emis.common.server.publishapi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by thomas on 10/08/2017.
 */
public class MethodValues {

    @SerializedName("downloadMethods")
    public List<DownloadMethod> downloadMethods;
    @SerializedName("publishMethods")
    public List<PublishMethod> publishMethods;
    @SerializedName("platforms")
    public List<Platform> platforms;
    @SerializedName("freePlatforms")
    public List<String> freePlatforms;

    public static class DownloadMethod {
        @SerializedName("platforms")
        public List<String> platforms;
        @SerializedName("downloadMethod")
        public String downloadMethod;
    }

    public static class PublishMethod {
        @SerializedName("platforms")
        public List<String> platforms;
        @SerializedName("productType")
        public String productType;
        @SerializedName("process")
        public String process;
        @SerializedName("processId")
        public Long processId;
    }

    public static class Platform {
        @SerializedName("name")
        public String name;
        @SerializedName("values")
        public List<String> values;
    }
}
