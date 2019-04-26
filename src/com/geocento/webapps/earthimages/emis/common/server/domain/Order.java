package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "orders")
//@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"name", "owner.username"})})
public class Order {

	@Id
	String id;

	@ManyToOne
    User owner;

    @Enumerated(EnumType.STRING)
    ORDER_STATUS status;

    @Column(length = 100)
    String name;
    @Column(length = 1000)
    String description;

    // list of the cart product requests
    @OneToMany(mappedBy = "order", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private List<ProductOrder> productOrders = new ArrayList<ProductOrder>();

    @Temporal(TemporalType.TIMESTAMP)
    Date creationTime;
    @Temporal(TemporalType.TIMESTAMP)
    Date lastUpdate;

    // list of orders ids
    @ElementCollection
    private List<String> EIOrderIds;

    @ManyToMany(fetch = FetchType.LAZY)
    List<SignedLicense> signedLicenses;

    // list of signed licenses

    public Order() {
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public ORDER_STATUS getStatus() {
        return status;
    }

    public void setStatus(ORDER_STATUS status) {
        this.status = status;
    }

    public List<ProductOrder> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrder> productOrders) {
        this.productOrders = productOrders;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getEIOrderIds() {
        return EIOrderIds;
    }

    public void setEIOrderIds(List<String> EIOrders) {
        this.EIOrderIds = EIOrders;
    }

    public List<SignedLicense> getSignedLicenses() {
        return signedLicenses;
    }

    public void setSignedLicenses(List<SignedLicense> signedLicenses) {
        this.signedLicenses = signedLicenses;
    }
}
