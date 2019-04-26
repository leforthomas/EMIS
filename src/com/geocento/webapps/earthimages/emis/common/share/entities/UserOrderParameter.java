package com.geocento.webapps.earthimages.emis.common.share.entities;

import com.metaaps.webapps.libraries.client.property.domain.Property;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class UserOrderParameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	Long id;

    // keep track of the original order parameter
    Long orderParameterId;

	@Column(length=255)
	String name;

    @Column(length=100)
    String category;

    @Column(length=255)
	String propertyValue;

	@Embedded
	Price price;
	
	public UserOrderParameter() {
	}

	// define a new order parameter based on a property
	public UserOrderParameter(Property property, Price price) {
		super();
		this.name = property.getName();
		this.propertyValue = property.getValue().toString();
		this.price = price;
	}

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Price getPrice() {
		return price;
	}
	
	public void setPrice(Price price) {
		this.price = price;
	}

    public Long getOrderParameterId() {
        return orderParameterId;
    }

    public void setOrderParameterId(Long orderParameterId) {
        this.orderParameterId = orderParameterId;
    }

}
