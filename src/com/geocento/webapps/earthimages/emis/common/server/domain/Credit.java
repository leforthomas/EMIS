package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.List;

@Entity
public class Credit {

    @Id
    @GeneratedValue
    Long id;

    @OneToOne(mappedBy = "credit")
    User owner;

    double current;
    String currency;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL)
    List<Transaction> transactions;

    public Credit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
