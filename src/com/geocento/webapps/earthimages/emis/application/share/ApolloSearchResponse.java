package com.geocento.webapps.earthimages.emis.application.share;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by thomas on 21/07/2017.
 */
public class ApolloSearchResponse implements Serializable{

    public final static String AGGREGATE_CLASS = "com.erdas.rsp.babel.model.ResourceAggregate";

    public ApolloSearchResponse()
    {

    }

    @JsonProperty("_encodingVersion")
    public String _encodingVersion;
    @JsonProperty("_encodingTime")
    public int _encodingTime;
    @JsonProperty("context")
    public Context context;

    @JsonProperty("results")
    public Result [] results;

    public static class QueryParameters implements Serializable{
        @JsonProperty("maxresults")
        public String maxResults;

        @JsonProperty("keywords")
        public String keywords;

        @JsonProperty("orderby")
        public String orderBy;

        @JsonProperty("start")
        public String start;

        @JsonProperty("intersect")
        public String intersect;

        @JsonProperty("inurl")
        public String inurl;

    }

    public static class Context implements Serializable{

        @JsonProperty("totalAvailableResults")
        public int totalAvailableResults;

        @JsonProperty("queryParameters")
        public QueryParameters queryParameters;

        @JsonProperty("maxResults")
        public int maxResults;

        @JsonProperty("startIdx")
        public int startIdx;

        @JsonProperty("queryTimeMillis")
        public int queryTimeMillis;

        @JsonProperty("rootPath")
        public String rootPath;

        @JsonProperty("_class")
        public String _class;
    }

    public static class Result implements Serializable{

        @JsonProperty("identifier")
        public String identifier;

        @JsonProperty("defaultAttachmentName")
        public String defaultAttachmentName;

        @JsonProperty("name")
        public String name;

        @JsonProperty("description")
        public String description;

        @JsonProperty("footprint")
        public Footprint footprint;

        @JsonProperty("id")
        public String id;

        @JsonProperty("_class")
        public String _class;

        @JsonProperty("title")
        public String title;

        @JsonProperty("parentId")
        public String parentId;

        @JsonProperty("tags")
        public String [] tags;

    }

    public static class Footprint implements Serializable
    {
        @JsonProperty("envelope")
        public List<String> envelope;
        @JsonProperty("epsgId")
        public int epsgId;
        @JsonProperty("data")
        public List<double []> data;
        @JsonProperty("srs")
        public String srs;
        @JsonProperty("type")
        public String type;
        @JsonProperty("cardinality")
        public int cardinality;
    }


}
