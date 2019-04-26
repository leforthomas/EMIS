package com.geocento.webapps.earthimages.emis.application.share;

import com.metaaps.webapps.libraries.client.map.EOLatLng;

import java.io.Serializable;
import java.util.List;

/**
 * Created by thomas on 04/05/2017.
 */
public class SampleDTO implements Serializable {

    String id;
    String name;
    String description;
    String platformName;
    String thumbnail;
    List<EOLatLng> coordinates;
    ProductMetadataDTO productMetadataDTO;
    Long productFileSizeBytes;
    private String productWMSServiceURL;
    private List<String> keywords;

    boolean visible;
    boolean displayImage;
    boolean imageLoading;
    private String platform;
    private String productFileName;
    private String productOrderId;
    private String downloadManualURL;
    private String originalProductId;
    private boolean highlighted;
    private Long instrumentId;

    public SampleDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<EOLatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<EOLatLng> coordinates) {
        this.coordinates = coordinates;
    }

    public ProductMetadataDTO getProductMetadataDTO() {
        return productMetadataDTO;
    }

    public void setProductMetadataDTO(ProductMetadataDTO productMetadataDTO) {
        this.productMetadataDTO = productMetadataDTO;
    }

    public Long getProductFileSizeBytes() {
        return productFileSizeBytes;
    }

    public void setProductFileSizeBytes(Long productFileSizeBytes) {
        this.productFileSizeBytes = productFileSizeBytes;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getProductWMSServiceURL() {
        return productWMSServiceURL;
    }

    public void setProductWMSServiceURL(String productWMSServiceURL) {
        this.productWMSServiceURL = productWMSServiceURL;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public boolean isDisplayImage() {
        return displayImage;
    }

    public void setDisplayImage(boolean displayImage) {
        this.displayImage = displayImage;
    }

    public boolean isImageLoading() {
        return imageLoading;
    }

    public void setImageLoading(boolean imageLoading) {
        this.imageLoading = imageLoading;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getProductFileName() {
        return productFileName;
    }

    public void setProductFileName(String productFileName) {
        this.productFileName = productFileName;
    }

    public void setProductOrderId(String productOrderId) {
        this.productOrderId = productOrderId;
    }

    public String getProductOrderId() {
        return productOrderId;
    }

    public String getDownloadManualURL() {
        return downloadManualURL;
    }

    public void setDownloadManualURL(String downloadManualURL) {
        this.downloadManualURL = downloadManualURL;
    }

    public String getOriginalProductId() {
        return originalProductId;
    }

    public void setOriginalProductId(String originalProductId) {
        this.originalProductId = originalProductId;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }
}
