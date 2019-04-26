package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;

import java.util.List;

/**
 * Created by thomas on 27/01/2017.
 */
public class ProductDisplay {

    Product product;
    boolean visible;
    boolean displayImage;
    boolean imageLoading;
    private boolean showInfo;
    private EOLatLng center;
    private EOBounds bounds;
    private EOLatLng[] coordinates;
    private com.geocento.webapps.earthimages.emis.common.share.entities.AOI aoi;
    private int stripCount;
    private List<EOLatLng> stripContour;
    private boolean showStripInfo;
    private List<ProductOrderDTO> productOrders;
    private boolean showOrderInfo;
    private boolean showProductOrders;

    public ProductDisplay() {
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
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

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }

    public EOLatLng getCenter() {
        return center;
    }

    public void setCenter(EOLatLng center) {
        this.center = center;
    }

    public EOBounds getBounds() {
        return bounds;
    }

    public void setBounds(EOBounds bounds) {
        this.bounds = bounds;
    }

    public EOLatLng[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(EOLatLng[] coordinates) {
        this.coordinates = coordinates;
    }


    public AOI getAoi() {
        return aoi;
    }

    public void setAoi(AOI aoi) {
        this.aoi = aoi;
    }

    public void setStripCount(int stripCount) {
        this.stripCount = stripCount;
    }

    public int getStripCount() {
        return stripCount;
    }

    public void setStripContour(List<EOLatLng> stripContour) {
        this.stripContour = stripContour;
    }

    public List<EOLatLng> getStripContour() {
        return stripContour;
    }

    public boolean isShowStripInfo() {
        return showStripInfo;
    }

    public void setShowStripInfo(boolean showStripInfo) {
        this.showStripInfo = showStripInfo;
    }

    public List<ProductOrderDTO> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrderDTO> productOrders) {
        this.productOrders = productOrders;
    }

    public boolean isShowOrderInfo() {
        return showOrderInfo;
    }

    public void setShowOrderInfo(boolean showOrderInfo) {
        this.showOrderInfo = showOrderInfo;
    }

    public void setShowProductOrders(boolean showProductOrders) {
        this.showProductOrders = showProductOrders;
    }

    public boolean isShowProductOrders() {
        return showProductOrders;
    }
}
