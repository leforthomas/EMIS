package com.geocento.webapps.earthimages.emis.common.share;


import java.io.Serializable;

public interface Resource extends Serializable {
	
	String getName();
	
	ResourceIcons getIcons();

	Long getId();

}
