package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.entities.TRANSACTION_TYPE;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Transaction {

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    Credit credit;

    @Enumerated(EnumType.STRING)
    TRANSACTION_TYPE transactionType;

    // only for credit card type of transaction
    @Column(length = 255)
    String braintreeTransactionId;

    double amount;
    @Column(length = 3)
    String currency;

    // only for adding funds type of transaction
    @Column(length = 255)
    String invoiceId;

    // only for product order payments
    @ManyToOne
    Order order;

    @Column(length = 1000)
    String comment;

    @Temporal(TemporalType.TIMESTAMP)
    Date date;

    public Transaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Credit getCredit() {
        return credit;
    }

    public void setCredit(Credit credit) {
        this.credit = credit;
    }

    public TRANSACTION_TYPE getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TRANSACTION_TYPE transactionType) {
        this.transactionType = transactionType;
    }

    public String getBraintreeTransactionId() {
        return braintreeTransactionId;
    }

    public void setBraintreeTransactionId(String braintreeTransactionId) {
        this.braintreeTransactionId = braintreeTransactionId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }
}
