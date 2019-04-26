package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;

public class UserOrderParameterDTO implements Serializable {

	Long id;
	String name;
	String value;

	public UserOrderParameterDTO() {
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
