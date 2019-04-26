package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PUBLICATION_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.metaaps.webapps.libraries.client.map.EOLatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 04/05/2017.
 */
public class ProductOrderDTO implements Serializable {

    private String id;
    private AOI aoi;
    private String description;
    private EOLatLng[] coordinates;
    private PRODUCTORDER_STATUS status;
    private Date estimatedDeliveryDate;
    private String thumbnailURL;
    private String info;
    private boolean visible;
    private String productWMSServiceURL;
    private String originalProductId;
    private ArrayList<ProductMetadataDTO> publishedProducts;
    private PUBLICATION_STATUS publicationStatus;
    private List<WorkspaceSummaryDTO> workspaces;
    private Date originalProductAcquisitionTime;
    private String productFileName;
    private Long productFileSizeBytes;
    private String label;
    private Date deliveredDate;
    private Long policyId;
    private String EIOrderId;
    private Price paidPrice;
    private Price offeredPrice;
    private Date paidDate;
    private Price totalPrice;
    private Price convertedOfferedPrice;
    private String downloadManualURL;
    private String license;
    private List<UserOrderParameterDTO> parameters;
    private String comments;

    public ProductOrderDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AOI getAOI() {
        return aoi;
    }

    public void setAOI(AOI AOI) {
        this.aoi = AOI;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EOLatLng[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(EOLatLng[] coordinates) {
        this.coordinates = coordinates;
    }

    public PRODUCTORDER_STATUS getStatus() {
        return status;
    }

    public void setStatus(PRODUCTORDER_STATUS status) {
        this.status = status;
    }

    public Date getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getProductWMSServiceURL() {
        return productWMSServiceURL;
    }

    public void setProductWMSServiceURL(String productWMSServiceURL) {
        this.productWMSServiceURL = productWMSServiceURL;
    }

    public String getOriginalProductId() {
        return originalProductId;
    }

    public void setOriginalProductId(String originalProductId) {
        this.originalProductId = originalProductId;
    }

    public void setPublishedProducts(ArrayList<ProductMetadataDTO> publishedProducts) {
        this.publishedProducts = publishedProducts;
    }

    public ArrayList<ProductMetadataDTO> getPublishedProducts() {
        return publishedProducts;
    }

    public void setPublicationStatus(PUBLICATION_STATUS publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public PUBLICATION_STATUS getPublicationStatus() {
        return publicationStatus;
    }

    public List<WorkspaceSummaryDTO> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<WorkspaceSummaryDTO> workspaces) {
        this.workspaces = workspaces;
    }

    public void setOriginalProductAcquisitionTime(Date originalProductAcquisitionTime) {
        this.originalProductAcquisitionTime = originalProductAcquisitionTime;
    }

    public Date getOriginalProductAcquisitionTime() {
        return originalProductAcquisitionTime;
    }

    public String getProductFileName() {
        return productFileName;
    }

    public void setProductFileName(String productFileName) {
        this.productFileName = productFileName;
    }

    public Long getProductFileSizeBytes() {
        return productFileSizeBytes;
    }

    public void setProductFileSizeBytes(Long productFileSizeBytes) {
        this.productFileSizeBytes = productFileSizeBytes;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setDeliveredDate(Date deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public Date getDeliveredDate() {
        return deliveredDate;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setEIOrderId(String EIOrderId) {
        this.EIOrderId = EIOrderId;
    }

    public String getEIOrderId() {
        return EIOrderId;
    }

    public void setPaidPrice(Price paidPrice) {
        this.paidPrice = paidPrice;
    }

    public Price getPaidPrice() {
        return paidPrice;
    }

    public Price getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(Price offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public Date getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Date paidDate) {
        this.paidDate = paidDate;
    }

    public Price getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Price totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Price getConvertedOfferedPrice() {
        return convertedOfferedPrice;
    }

    public void setConvertedOfferedPrice(Price convertedOfferedPrice) {
        this.convertedOfferedPrice = convertedOfferedPrice;
    }

    public String getDownloadManualURL() {
        return downloadManualURL;
    }

    public void setDownloadManualURL(String downloadManualURL) {
        this.downloadManualURL = downloadManualURL;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public List<UserOrderParameterDTO> getParameters() {
        return parameters;
    }

    public void setParameters(List<UserOrderParameterDTO> parameters) {
        this.parameters = parameters;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }
}
