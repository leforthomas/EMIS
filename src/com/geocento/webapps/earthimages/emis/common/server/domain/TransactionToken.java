package com.geocento.webapps.earthimages.emis.common.server.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by thomas on 04/07/2014.
 */
public class TransactionToken implements Serializable {

    private String id;
    private double amount;
    private Date createdAt;
    private String currency;
    private String status;

    public TransactionToken() {
    }

    public TransactionToken(String id, double amount, Date createdAt, String currency, String status) {
        this.id = id;
        this.amount = amount;
        this.createdAt = createdAt;
        this.currency = currency;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }
}
