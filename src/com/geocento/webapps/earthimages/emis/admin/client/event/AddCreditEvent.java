package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.common.share.entities.TRANSACTION_TYPE;
import com.google.gwt.event.shared.GwtEvent;

public class AddCreditEvent extends GwtEvent<AddCreditEventHandler> {

    public static Type<AddCreditEventHandler> TYPE = new Type<AddCreditEventHandler>();

    private final String username;
    private double amount;
    private String currency;
    private String comment;
    private TRANSACTION_TYPE transactionType;

    public AddCreditEvent(String username, TRANSACTION_TYPE transactionType, double amount, String currency, String comment) {
        this.username = username;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.comment = comment;
    }

    public String getUsername() {
        return username;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Type<AddCreditEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(AddCreditEventHandler handler) {
        handler.onAddCredit(this);
    }

    public TRANSACTION_TYPE getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TRANSACTION_TYPE transactionType) {
        this.transactionType = transactionType;
    }
}
