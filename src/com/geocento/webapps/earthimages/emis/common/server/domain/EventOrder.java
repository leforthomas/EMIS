package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(name = "event")
public class EventOrder {

    // the event ID which is also the UID from the event API
	@Id
	String id;

    // list of the products which have been acquired for this event
    @OneToMany(mappedBy = "eventOrder", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private List<ProductOrder> productsOrdered = new ArrayList<ProductOrder>();

    @Temporal(TemporalType.TIMESTAMP)
    Date creationTime;
    @Temporal(TemporalType.TIMESTAMP)
    Date lastUpdate;

    public EventOrder() {
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ProductOrder> getProductsOrdered() {
        return productsOrdered;
    }

    public void setProductsOrdered(List<ProductOrder> productOrders) {
        this.productsOrdered = productOrders;
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
}
