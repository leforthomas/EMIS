package com.geocento.webapps.earthimages.emis.common.share;

import java.io.Serializable;

public class LayerStyle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;

	public LayerStyle() {
	}
	
	public LayerStyle(String name) {
		this.name = name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
