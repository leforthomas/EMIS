package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;

public class CreditCardRequest implements Serializable {

    String token;

    String number;
    String cvv;
    String month;
    String year;

    boolean storeCard;

    public CreditCardRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public boolean isStoreCard() {
        return storeCard;
    }

    public void setStoreCard(boolean storeCard) {
        this.storeCard = storeCard;
    }
}
