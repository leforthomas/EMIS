package com.geocento.webapps.earthimages.emis.admin.share;

import java.io.Serializable;

public class CreditDTO implements Serializable {

    double amount;
    String currency;

    public CreditDTO() {
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
