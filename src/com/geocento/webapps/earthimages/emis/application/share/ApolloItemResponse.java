package com.geocento.webapps.earthimages.emis.application.share;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApolloItemResponse implements Serializable
{

    @JsonProperty("_encodingVersion")
    public String _encodingVersion;
    @JsonProperty("_encodingTime")
    public int _encodingTime;
    @JsonProperty("context")
    public Context context;
    @JsonProperty("results")
    public List<Results> results;


    public ApolloItemResponse()
    {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QueryParameters implements Serializable
    {
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Context implements Serializable
    {
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parent implements Serializable
    {
        @JsonProperty("identifier")
        public String identifier;
        @JsonProperty("defaultAttachmentName")
        public String defaultAttachmentName;
        @JsonProperty("name")
        public String name;
        @JsonProperty("id")
        public String id;
        @JsonProperty("title")
        public String title;
        @JsonProperty("parentId")
        public String parentId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SPECIAL implements Serializable
    {
        @JsonProperty("username")
        public String username;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class METADATA_URI implements Serializable
    {
        @JsonProperty("ISO19115 Writer using templates")
        public String ISO19115_Writer_using_templates;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DEFAULT implements Serializable
    {
        @JsonProperty("ionicQueryableNamesProp")
        public String ionicQueryableNamesProp;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata implements Serializable
    {
        @JsonProperty("METADATA_URI")
        public METADATA_URI METADATA_URI;
        @JsonProperty("SPECIAL")
        public SPECIAL SPECIAL;
        @JsonProperty("DEFAULT")
        public DEFAULT DEFAULT;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AcquisitionInfo implements Serializable
    {
        @JsonProperty("metadataAcquired")
        public boolean metadataAcquired;
        @JsonProperty("dataProcessed")
        public boolean dataProcessed;
        @JsonProperty("acquisitionDate")
        public String acquisitionDate;
        @JsonProperty("lastModificationDate")
        public String lastModificationDate;
        @JsonProperty("dataAcquired")
        public boolean dataAcquired;
        @JsonProperty("processingLevel")
        public String processingLevel;
        @JsonProperty("qualityValue")
        public int qualityValue;
        @JsonProperty("errorValue")
        public int errorValue;
        @JsonProperty("acquisitionLevelMask")
        public int acquisitionLevelMask;
        @JsonProperty("availabilityDate")
        public String availabilityDate;
        @JsonProperty("resolution")
        public int resolution;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClassifyingConcepts implements Serializable
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Children implements Serializable
    {
        @JsonProperty("identifier")
        public String identifier;

        @JsonProperty("defaultAttachmentName")
        public String defaultAttachmentName;

        @JsonProperty("name")
        public String name;

        @JsonProperty("id")
        public String id;

        @JsonProperty("_class")
        public String _class;

        @JsonProperty("title")
        public String title;

        @JsonProperty("parentId")
        public String parentId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssociationsAsSource implements Serializable
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tags implements Serializable
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssociationsAsTarget implements Serializable
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Thumbnail implements Serializable
    {
        @JsonProperty("lastUpdated")
        public String lastUpdated;
        @JsonProperty("path")
        public String path;
        @JsonProperty("name")
        public String name;
        @JsonProperty("mimeType")
        public String mimeType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MDefault implements Serializable
    {
        @JsonProperty("lastUpdated")
        public String lastUpdated;
        @JsonProperty("name")
        public String name;
        @JsonProperty("mimeType")
        public String mimeType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachments implements Serializable
    {
        @JsonProperty("thumbnail")
        public Thumbnail thumbnail;
        @JsonProperty("default")
        public MDefault mdefault;
        @JsonProperty("default_vector_style")
        public MDefault default_vector_style;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NativeFootprint implements Serializable
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Maximum implements Serializable
    {
        @JsonProperty("string")
        public String string;
        @JsonProperty("typeName")
        public String typeName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Resolution implements Serializable
    {
        @JsonProperty("string")
        public String string;
        @JsonProperty("typeName")
        public String typeName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Minimum implements Serializable
    {
        @JsonProperty("string")
        public String string;
        @JsonProperty("typeName")
        public String typeName;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RangeExtent implements Serializable
    {
        @JsonProperty("continuousRange")
        public boolean continuousRange;
        @JsonProperty("size")
        public int size;
        @JsonProperty("maximum")
        public Maximum maximum;
        @JsonProperty("singleValue")
        public boolean singleValue;
        @JsonProperty("resolution")
        public Resolution resolution;
        @JsonProperty("minimum")
        public Minimum minimum;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RangeAxisList implements Serializable
    {
        @JsonProperty("channelRange")
        public boolean channelRange;
        @JsonProperty("rangeExtent")
        public List<RangeExtent> rangeExtent;
        @JsonProperty("name")
        public String name;
        @JsonProperty("namespace")
        public String namespace;
        @JsonProperty("abstract")
        public String mabstract;
        @JsonProperty("_class")
        public String _class;
        @JsonProperty("title")
        public String title;
        @JsonProperty("nullValue")
        public boolean nullValue;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RangeSetSequence implements Serializable
    {
        @JsonProperty("name")
        public String name;
        @JsonProperty("namespace")
        public String namespace;
        @JsonProperty("rangeAxisList")
        public List<RangeAxisList> rangeAxisList;
        @JsonProperty("iD")
        public String iD;
        @JsonProperty("nameOrID")
        public String nameOrID;
        @JsonProperty("abstract")
        public String mabstract;
        @JsonProperty("_class")
        public String _class;
        @JsonProperty("title")
        public String title;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RangeSetDescription implements Serializable
    {
        @JsonProperty("rangeSetSequence")
        public List<RangeSetSequence> rangeSetSequence;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Security implements Serializable
    {
        @JsonProperty("shared")
        public boolean shared;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegistryPackages implements Serializable
    {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sizes implements Serializable
    {
        @JsonProperty("width")
        public int width;
        @JsonProperty("height")
        public int height;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LevelDescriptors implements Serializable
    {
        @JsonProperty("sizes")
        public Sizes sizes;
        @JsonProperty("index")
        public int index;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PyramidDescriptor implements Serializable
    {
        @JsonProperty("levelCount")
        public int levelCount;
        @JsonProperty("internal")
        public boolean internal;
        @JsonProperty("upToDate")
        public boolean upToDate;
        @JsonProperty("sizes")
        public Sizes sizes;
        @JsonProperty("decimationFactor")
        public int decimationFactor;
        @JsonProperty("aggregatePyramid")
        public boolean aggregatePyramid;
        @JsonProperty("levelDescriptors")
        public List<LevelDescriptors> levelDescriptors;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YExtent implements Serializable
    {
        @JsonProperty("continuousRange")
        public boolean continuousRange;
        @JsonProperty("size")
        public int size;
        @JsonProperty("maximum")
        public Maximum maximum;
        @JsonProperty("singleValue")
        public boolean singleValue;
        @JsonProperty("resolution")
        public Resolution resolution;
        @JsonProperty("minimum")
        public Minimum minimum;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Srs implements Serializable
    {
        @JsonProperty("epsgid")
        public int epsgid;
        @JsonProperty("srs")
        public String srs;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoundingGeometry implements Serializable
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class XExtent implements Serializable
    {
        @JsonProperty("continuousRange")
        public boolean continuousRange;
        @JsonProperty("size")
        public int size;
        @JsonProperty("maximum")
        public Maximum maximum;
        @JsonProperty("singleValue")
        public boolean singleValue;
        @JsonProperty("resolution")
        public Resolution resolution;
        @JsonProperty("minimum")
        public Minimum minimum;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SpatialExtent implements Serializable
    {
        @JsonProperty("yExtent")
        public YExtent yExtent;
        @JsonProperty("zExtent")
        public String zExtent;
        @JsonProperty("pixelSpace")
        public boolean pixelSpace;
        @JsonProperty("dataType")
        public int dataType;
        @JsonProperty("latLongBoundingBox")
        public List<String> latLongBoundingBox;
        @JsonProperty("verticalDatum")
        public String verticalDatum;
        @JsonProperty("dataProcessingLevel")
        public int dataProcessingLevel;
        @JsonProperty("georeferenced")
        public boolean georeferenced;
        @JsonProperty("envelope")
        public List<String> envelope;
        @JsonProperty("srs")
        public Srs srs;
        @JsonProperty("georectified")
        public boolean georectified;
        @JsonProperty("boundingGeometry")
        public BoundingGeometry boundingGeometry;
        @JsonProperty("xExtent")
        public XExtent xExtent;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DomainSetExtent implements Serializable
    {
        @JsonProperty("elevationExtent")
        public String elevationExtent;
        @JsonProperty("spatialExtent")
        public SpatialExtent spatialExtent;
        @JsonProperty("temporalExtent")
        public TemporalExtent temporalExtent;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SerializedObject implements Serializable
    {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties implements Serializable
    {
        @JsonProperty("apollo.connector")
        public String apolloConnector;

        @JsonProperty("generateAggregateThumbnails")
        public String generateAggregateThumbnails;
        @JsonProperty("generateAggregateISOFile")
        public String generateAggregateISOFile;
        @JsonProperty("publishIsAllowed")
        public PublishIsAllowed publishIsAllowed;
        //@JsonProperty("downloadIsAllowed")
        //public DownloadIsAllowed downloadIsAllowed;
        //public SerializedObject downloadIsAllowed;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PublishIsAllowed implements Serializable
        {
            @JsonProperty("val")
            public String val;
            @JsonProperty("_class")
            public String _class;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DownloadIsAllowed implements Serializable
        {
            @JsonProperty("val")
            public String val;
            @JsonProperty("_class")
            public String _class;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Results implements Serializable
    {
        @JsonProperty("parent")
        public Parent parent;
        @JsonProperty("metadata")
        public Metadata metadata;
        @JsonProperty("downloadEnabled")
        public boolean downloadEnabled;
        @JsonProperty("acquisitionInfo")
        public AcquisitionInfo acquisitionInfo;
        @JsonProperty("publishIsAllowed")
        public boolean publishIsAllowed;
        @JsonProperty("geoServicesEnabled")
        public boolean geoServicesEnabled;
        @JsonProperty("storageStatus")
        public String storageStatus;
        @JsonProperty("classifyingConcepts")
        public List<ClassifyingConcepts> classifyingConcepts;
        @JsonProperty("metadataURI")
        public String metadataURI;
        @JsonProperty("path")
        public String path;
        @JsonProperty("wmsEnabled")
        public boolean wmsEnabled;
        @JsonProperty("wcsEnabled")
        public boolean wcsEnabled;
        @JsonProperty("wfstEnabled")
        public boolean wfstEnabled;
        @JsonProperty("children")
        public List<Children> children;
        @JsonProperty("registrationDate")
        public String registrationDate;
        @JsonProperty("id")
        public String id;
        @JsonProperty("spatialExtentZPixelSize")
        public String spatialExtentZPixelSize;
        @JsonProperty("viewEnabled")
        public boolean viewEnabled;
        @JsonProperty("identifier")
        public String identifier;
        @JsonProperty("associationsAsSource")
        public List<AssociationsAsSource> associationsAsSource;
        @JsonProperty("jpipEnabled")
        public boolean jpipEnabled;
        @JsonProperty("temporalExtentStartDate")
        public String temporalExtentStartDate;
        @JsonProperty("wmtsEnabled")
        public boolean wmtsEnabled;
        @JsonProperty("creationDate")
        public String creationDate;
        @JsonProperty("tags")
        //public List<Tags> tags;
        public String [] tags;
        @JsonProperty("drawOrder")
        public int drawOrder;
        @JsonProperty("czsEnabled")
        public boolean czsEnabled;
        @JsonProperty("defaultAttachmentName")
        public String defaultAttachmentName;
        @JsonProperty("name")
        public String name;
        @JsonProperty("onlineStoragePath")
        public String onlineStoragePath;
        @JsonProperty("_class")
        public String _class;
        @JsonProperty("ogcUniqueName")
        public String ogcUniqueName;
        @JsonProperty("providerConfig")
        public String providerConfig;
        @JsonProperty("favorite")
        public boolean favorite;
        @JsonProperty("associationsAsTarget")
        public List<AssociationsAsTarget> associationsAsTarget;
        @JsonProperty("spatialExtentYPixelSize")
        public int spatialExtentYPixelSize;
        @JsonProperty("temporalExtentEndDate")
        public String temporalExtentEndDate;
        @JsonProperty("wmtsLayerName")
        public String wmtsLayerName;
        @JsonProperty("attachments")
        public Attachments attachments;
        @JsonProperty("hidden")
        public boolean hidden;
        @JsonProperty("nativeFootprint")
        public NativeFootprint nativeFootprint;
        @JsonProperty("description")
        public String description;
        @JsonProperty("rangeSetDescription")
        public RangeSetDescription rangeSetDescription;
        @JsonProperty("ecwpLayerName")
        public String ecwpLayerName;
        @JsonProperty("title")
        public String title;
        @JsonProperty("connectorClass")
        public String connectorClass;
        @JsonProperty("offlineStoragePath")
        public String offlineStoragePath;
        @JsonProperty("security")
        public Security security;
        @JsonProperty("nameQualifier")
        public String nameQualifier;
        @JsonProperty("fileURI")
        public String fileURI;
        @JsonProperty("externalBucketName")
        public String externalBucketName;
        @JsonProperty("registryPackages")
        public List<RegistryPackages> registryPackages;
        @JsonProperty("imageXEnabled")
        public boolean imageXEnabled;
        @JsonProperty("activeStoragePath")
        public String activeStoragePath;
        @JsonProperty("pyramidDescriptor")
        public PyramidDescriptor pyramidDescriptor;
        /*@JsonProperty("downloadIsAllowed")
        public boolean downloadIsAllowed;*/
        @JsonProperty("parentId")
        public String parentId;
        @JsonProperty("spatialExtentXPixelSize")
        public int spatialExtentXPixelSize;
        @JsonProperty("modificationDate")
        public String modificationDate;
        @JsonProperty("decoderName")
        public String decoderName;
        @JsonProperty("footprint")
        public Footprint footprint;
        @JsonProperty("ecwpEnabled")
        public boolean ecwpEnabled;
        @JsonProperty("domainSetExtent")
        public DomainSetExtent domainSetExtent;
        /*@JsonProperty("dBConnectionConfig")
        public String dBConnectionConfig;*/
        @JsonProperty("properties")
        public Properties properties;


        @JsonProperty("numberOfFeatures")
        public int numberOfFeatures;
        @JsonProperty("attributeDefinitions")
        public List<AttributeDefinitions> attributeDefinitions;
        @JsonProperty("wfsEnabled")
        public boolean wfsEnabled;
        @JsonProperty("sourceFormat")
        public String sourceFormat;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class AttributeDefinitions implements Serializable
        {
            @JsonProperty("attributeIsEnabled")
            public boolean attributeIsEnabled;
            @JsonProperty("attributeDisplayName")
            public String attributeDisplayName;
            @JsonProperty("attributeType")
            public String attributeType;
            @JsonProperty("attributeOgcName")
            public String attributeOgcName;
            @JsonProperty("attributeName")
            public String attributeName;
            @JsonProperty("primaryGeometry")
            public boolean primaryGeometry;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Default_vector_style implements Serializable
        {
            @JsonProperty("lastUpdated")
            public String lastUpdated;
            @JsonProperty("name")
            public String name;
            @JsonProperty("mimeType")
            public String mimeType;
        }

    }


    public static class FullExtent implements Serializable
    {
        @SerializedName("continuousRange")
        public boolean continuousRange;
        @SerializedName("size")
        public int size;
        @SerializedName("maximum")
        public Maximum maximum;
        @SerializedName("singleValue")
        public boolean singleValue;
        @SerializedName("minimum")
        public Minimum minimum;
        @SerializedName("resolution")
        public Resolution resolution;
    }



    public static class Extent implements Serializable
    {
        @SerializedName("continuousRange")
        public boolean continuousRange;
        @SerializedName("size")
        public int size;
        @SerializedName("maximum")
        public Maximum maximum;
        @SerializedName("singleValue")
        public boolean singleValue;
        @SerializedName("minimum")
        public Minimum minimum;
        @SerializedName("resolution")
        public Resolution resolution;
    }



    public static class TemporalExtent implements Serializable
    {
        @SerializedName("fullExtent")
        public FullExtent fullExtent;
        @SerializedName("extent")
        public List<Extent> extent;
        @SerializedName("fullExtentString")
        public String fullExtentString;
        @SerializedName("unit")
        public String unit;
        @SerializedName("extentValues")
        public List<String> extentValues;
        @SerializedName("formatedString")
        public String formatedString;
        @SerializedName("temporalExtent")
        public RangeExtent temporalExtent;
    }
}


