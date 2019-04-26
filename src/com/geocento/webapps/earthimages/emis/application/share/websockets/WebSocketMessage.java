package com.geocento.webapps.earthimages.emis.application.share.websockets;

import com.geocento.webapps.earthimages.emis.application.share.CartProductDTO;
import com.github.nmorel.gwtjackson.client.ObjectMapper;

import java.util.List;

/**
 * Created by thomas on 30/05/2017.
 */
public class WebSocketMessage {

    public static interface WebSocketMessageMapper extends ObjectMapper<WebSocketMessage> {};

    static public enum TYPE {productOrderNotification, cartChange, cartProductsChanged, workspaceProductPublished, creditUpdated, orderNotification, logout};

    TYPE type;

    // TODO - replace with child classes

    // list of possible actual values
    ProductOrderNotification productOrderNotification;
    List<CartProductDTO> cartProducts;
    WorkspaceProductPublishedNotification workspaceProductPublishedNotification;
    CreditUpdatedNotification creditUpdatedNotification;
    String orderId;

    public WebSocketMessage() {
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public ProductOrderNotification getProductOrderNotification() {
        return productOrderNotification;
    }

    public void setProductOrderNotification(ProductOrderNotification productOrderNotification) {
        this.productOrderNotification = productOrderNotification;
    }

    public void setCartProducts(List<CartProductDTO> cartProducts) {
        this.cartProducts = cartProducts;
    }

    public List<CartProductDTO> getCartProducts() {
        return cartProducts;
    }

    public void setWorkspaceProductPublishedNotification(WorkspaceProductPublishedNotification workspaceProductPublishedNotification) {
        this.workspaceProductPublishedNotification = workspaceProductPublishedNotification;
    }

    public WorkspaceProductPublishedNotification getWorkspaceProductPublishedNotification() {
        return workspaceProductPublishedNotification;
    }

    public CreditUpdatedNotification getCreditUpdatedNotification() {
        return creditUpdatedNotification;
    }

    public void setCreditUpdatedNotification(CreditUpdatedNotification creditUpdatedNotification) {
        this.creditUpdatedNotification = creditUpdatedNotification;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

}
