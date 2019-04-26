package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.UserOrderParameter;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.PRODUCT_SELECTION;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.ProductPolicy;
import com.metaaps.webapps.libraries.client.map.EOLatLng;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


public class ProductRequestDTO implements Serializable {

    Long id;
	Product product;

    AOI aoI;

    // the ordering policy for the selected product
    ProductPolicy policy;

    // the image selection
    PRODUCT_SELECTION selectionType;
    EOLatLng[] selectionGeometry;

    UserOrderParameter licenseOption;
    List<UserOrderParameter> orderingOptions;

    boolean orderable;
    String errorMessage;

    double imagePrice;
    double timePrice;
    double totalPrice;
    String currency;

	Date creationTime;
    private Price convertedTotalPrice;

    public ProductRequestDTO() {
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(ProductPolicy policy) {
        this.policy = policy;
    }

    public PRODUCT_SELECTION getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(PRODUCT_SELECTION selectionType) {
        this.selectionType = selectionType;
    }

    public EOLatLng[] getSelectionGeometry() {
        return selectionGeometry;
    }

    public void setSelectionGeometry(EOLatLng[] selectionGeometry) {
        this.selectionGeometry = selectionGeometry;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public AOI getAoI() {
        return aoI;
    }

    public void setAoI(AOI aoI) {
        this.aoI = aoI;
    }

    public void setLicenseOption(UserOrderParameter licenseOption) {
        this.licenseOption = licenseOption;
    }

    public UserOrderParameter getLicenseOption() {
        return licenseOption;
    }

    public List<UserOrderParameter> getOrderingOptions() {
        return orderingOptions;
    }

    public void setOrderingOptions(List<UserOrderParameter> orderingOptions) {
        this.orderingOptions = orderingOptions;
    }

    public double getImagePrice() {
        return imagePrice;
    }

    public void setImagePrice(double imagePrice) {
        this.imagePrice = imagePrice;
    }

    public double getTimePrice() {
        return timePrice;
    }

    public void setTimePrice(double timePrice) {
        this.timePrice = timePrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setOrderable(boolean orderable) {
        this.orderable = orderable;
    }

    public boolean isOrderable() {
        return orderable;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Price getConvertedTotalPrice() {
        return convertedTotalPrice;
    }

    public void setConvertedTotalPrice(Price convertedTotalPrice) {
        this.convertedTotalPrice = convertedTotalPrice;
    }
}
