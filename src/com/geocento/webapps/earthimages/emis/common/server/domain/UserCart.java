package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class UserCart {

	@Id
	@GeneratedValue
	Long id;

	@OneToOne
    User owner;
	
	// list of the cart product requests
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "userCart", cascade= CascadeType.ALL)
	private List<ProductRequest> productRequests = new ArrayList<ProductRequest>();

    // list of the cart product requests
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "userCart", cascade= CascadeType.ALL)
    private List<TaskingRequest> taskingRequests = new ArrayList<TaskingRequest>();

    // last update of the workspace by the user
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;
    
	public UserCart() {
		lastUpdate = new Date();
	}

	public Long getId() {
		return id;
	}
	
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	// TODO - check that product is not added twice
	public void addProductRequest(ProductRequest productRequest) {
		productRequests.add(productRequest);
	}
	
	public List<ProductRequest> getProductRequests() {
		return productRequests;
	}

    public void addTaskingRequest(TaskingRequest taskingRequest) {
        this.taskingRequests.add(taskingRequest);
    }

    public List<TaskingRequest> getTaskingRequests() {
        return taskingRequests;
    }
}
