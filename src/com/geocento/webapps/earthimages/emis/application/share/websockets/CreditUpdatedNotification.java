package com.geocento.webapps.earthimages.emis.application.share.websockets;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Created by thomas on 10/05/2017.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CreditUpdatedNotification {

    public double amount;
    public String currency;

    public CreditUpdatedNotification() {
    }

}
