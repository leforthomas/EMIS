package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.TRANSACTION_TYPE;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by thomas on 25/07/2014.
 */
public class PaymentTransactionDTO implements Serializable {

    Long id;
    TRANSACTION_TYPE type;
    String orderId;
    Price amount;
    String description;
    Date createdOn;
    private String invoiceUrl;

    public PaymentTransactionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TRANSACTION_TYPE getType() {
        return type;
    }

    public void setType(TRANSACTION_TYPE type) {
        this.type = type;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Price getAmount() {
        return amount;
    }

    public void setAmount(Price amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public void setInvoiceUrl(String invoiceUrl) {
        this.invoiceUrl = invoiceUrl;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }
}
