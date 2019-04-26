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
public class ProductRequest {

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

    // keep this to check whether the ordering policy has changed
    Long policyId;

    // the geocentoid for the product
    @Column(length = 100)
	String geocentoid;
    // the supplier id for the product
    @Column(length = 100)
    String providerId;

    // the provider name for the product
    @Column(length = 100)
    String providerName;

    // the basic product values
    @Column(length = 100)
    String satelliteName;
    @Column(length = 100)
    String sensorName;
    @Temporal(TemporalType.TIMESTAMP)
    Date start;
    @Column(length = 10000)
    String coordinatesWKT;

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

    public ProductRequest() {
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

    public String getGeocentoid() {
        return geocentoid;
    }

    public void setGeocentoid(String geocentoid) {
        this.geocentoid = geocentoid;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String supplierId) {
        this.providerId = supplierId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
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

    public String getSatelliteName() {
        return satelliteName;
    }

    public void setSatelliteName(String satelliteName) {
        this.satelliteName = satelliteName;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public String getCoordinatesWKT() {
        return coordinatesWKT;
    }

    public void setCoordinatesWKT(String coordinatesWKT) {
        this.coordinatesWKT = coordinatesWKT;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }
}