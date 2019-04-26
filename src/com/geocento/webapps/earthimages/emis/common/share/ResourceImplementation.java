package com.geocento.webapps.earthimages.emis.common.share;

import java.io.Serializable;

/** 
 * a ResourceImplementation is an implementation of a Resource
 * it is inserted in the view tree and provides methods for rendering in the tree
 * it can also provide methods for rendering in the map
 * it has user specific propertiesEditor not contained in the Resource object
 * it contains the original resource object
 *  
 * @author thomas
 *
 */
public interface ResourceImplementation<T extends Resource> extends Serializable {
	
	String getInstanceName();

	ResourceIcons getIcons();
	
	boolean isReady();

	boolean isSelected();
	
	void setSelected(boolean selected);
	
	T getResource();

}
