package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.SELECTION;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
public class TaskingRequest {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	Long id;

    @ManyToOne
    UserCart userCart;

    // the id for the search used to find the product
    @Column
    String searchId;

    Long policyId;

    // the product id generated using a hash to match two identical products
    @Column(length = 100)
    String productId;

    Long instrumentId;

    // the provider name for the product
    @Column(length = 100)
    String providerName;

    @OneToOne(cascade = CascadeType.ALL)
    ProductEntity productEntity;

	@ManyToOne
	@BatchFetch(value=BatchFetchType.JOIN)
    AOI aoi;

    // the actual requested AOI for the product
    // the image selection
    @Enumerated(EnumType.STRING)
    SELECTION selectionType;

    @Lob
    List<EOLatLng> selectionGeometry;

    /*
	 * creation time of the product request
	 */
	@Temporal(TemporalType.TIMESTAMP)
	Date creationTime;

    public TaskingRequest() {
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserCart getUserCart() {
        return userCart;
    }

    public void setUserCart(UserCart userCart) {
        this.userCart = userCart;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public ProductEntity getProductEntity() {
        return productEntity;
    }

    public void setProductEntity(ProductEntity productEntity) {
        this.productEntity = productEntity;
    }

    public AOI getAoi() {
        return aoi;
    }

    public void setAoi(AOI aoi) {
        this.aoi = aoi;
    }

    public SELECTION getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(SELECTION selectionType) {
        this.selectionType = selectionType;
    }

    public List<EOLatLng> getSelectionGeometry() {
        return selectionGeometry;
    }

    public void setSelectionGeometry(List<EOLatLng> selectionGeometry) {
        this.selectionGeometry = selectionGeometry;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }
}