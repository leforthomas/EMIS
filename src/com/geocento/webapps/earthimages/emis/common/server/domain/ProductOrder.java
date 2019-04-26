package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.server.utils.GeometryConverter;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
public class ProductOrder {

    @Id
	String id;

    @ManyToOne
    Order order;

    @Enumerated(EnumType.STRING)
    PRODUCTORDER_STATUS status;

    @Column(length = 1000)
    String title;

    @Column(length = 1000)
    String description;

    // used for grouping product orders together
    @Column(length = 100)
    String label;

    // linked to the search request one
	@ManyToOne(fetch=FetchType.LAZY)
	@BatchFetch(value=BatchFetchType.IN)
    AOI aoi;

    @Convert(converter = GeometryConverter.class)
    String selectionGeometry;

    // TODO - use the product request values instead?
    @OneToOne
    ProductRequest productRequest;
    @OneToOne
    TaskingRequest taskingRequest;

    // the idea of the ordering policy used for the ordering
    Long policyId;

    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    UserOrderParameter licenseOption;

    // list of parameters for the product
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@BatchFetch(value=BatchFetchType.IN)
	List<UserOrderParameter> parameters = new ArrayList<UserOrderParameter>();

	/*
	 * creation time of the product order
	 */
	@Temporal(TemporalType.TIMESTAMP)
	Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
	Date lastUpdate;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date estimatedDeliveryTime;
    @Temporal(TemporalType.TIMESTAMP)
    Date deliveredTime;

	// the latest price calculation
    Double totalPrice;
    Double offeredPrice;
	String currency;

    @Column(length = 10000)
    String comments;

    // the actual paid price in customer's currecny at the moment of the payment
    @Embedded
            @AttributeOverrides({
                    @AttributeOverride(name="value", column = @Column(name = "paidpricevalue")),
                    @AttributeOverride(name="currency", column = @Column(name = "paidpricecurrency"))
            })
    Price paidPrice;

    @Temporal(TemporalType.TIMESTAMP)
    Date paidDate;

    @Column(length = 1000)
    private String thumbnailURL;

    // TODO - move to other class?
    @Enumerated(EnumType.STRING)
    PUBLICATION_STATUS publicationStatus;

    @Column(length = 100)
    String layerName;

    @Column(length = 1000)
    String fileLocation;
    Long fileSize;

    // list of workspaces the product order has been added to
    @ManyToMany(fetch = FetchType.LAZY)
    List<Workspace> workspaces;
    // list of product processing published
    // first one is the display one by convention
    // TODO - make sure the order is kept
    @OneToMany(mappedBy = "productOrder", cascade = CascadeType.ALL)
    List<ProductPublishRequest> publishProductRequests;

    public ProductOrder() {
		creationTime = new Date();
	}

    public String getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PRODUCTORDER_STATUS getStatus() {
        return status;
    }

    public void setStatus(PRODUCTORDER_STATUS status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AOI getAoi() {
        return aoi;
    }

    public void setAoi(AOI aoi) {
        this.aoi = aoi;
    }

    public String getSelectionGeometry() {
        return selectionGeometry;
    }

    public void setSelectionGeometry(String selectionGeometry) {
        this.selectionGeometry = selectionGeometry;
    }

    public ProductRequest getProductRequest() {
        return productRequest;
    }

    public void setProductRequest(ProductRequest productRequest) {
        this.productRequest = productRequest;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public UserOrderParameter getLicenseOption() {
        return licenseOption;
    }

    public void setLicenseOption(UserOrderParameter licenseOption) {
        this.licenseOption = licenseOption;
    }

    public List<UserOrderParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<UserOrderParameter> parameters) {
        this.parameters = parameters;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(Date estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public Date getDeliveredTime() {
        return deliveredTime;
    }

    public void setDeliveredTime(Date deliveredTime) {
        this.deliveredTime = deliveredTime;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getOfferedPrice() {
        return offeredPrice;
    }

    public void setOfferedPrice(Double offeredPrice) {
        this.offeredPrice = offeredPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Price getPaidPrice() {
        return paidPrice;
    }

    public void setPaidPrice(Price paidPrice) {
        this.paidPrice = paidPrice;
    }

    public Date getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(Date paidDate) {
        this.paidDate = paidDate;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public PUBLICATION_STATUS getPublicationStatus() {
        return publicationStatus;
    }

    public void setPublicationStatus(PUBLICATION_STATUS publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public List<Workspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<Workspace> workspaces) {
        this.workspaces = workspaces;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public List<ProductPublishRequest> getPublishProductRequests() {
        return publishProductRequests;
    }

    public void setPublishProductRequests(List<ProductPublishRequest> publishProductRequests) {
        this.publishProductRequests = publishProductRequests;
    }

    public void setTaskingRequest(TaskingRequest taskingRequest) {
        this.taskingRequest = taskingRequest;
    }

    public TaskingRequest getTaskingRequest() {
        return taskingRequest;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }
}