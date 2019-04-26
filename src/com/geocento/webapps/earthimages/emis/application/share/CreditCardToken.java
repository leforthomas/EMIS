package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;

/**
 * Created by thomas on 04/07/2014.
 */
public class CreditCardToken implements Serializable {

    private String cardType;
    private String imageUrl;
    private String maskedNumber;
    private String token;

    public CreditCardToken() {
    }

    public CreditCardToken(String token, String cardType, String imageUrl, String maskedNumber) {
        this.token = token;
        this.cardType = cardType;
        this.imageUrl = imageUrl;
        this.maskedNumber = maskedNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMaskedNumber() {
        return maskedNumber;
    }

    public void setMaskedNumber(String maskedNumber) {
        this.maskedNumber = maskedNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
