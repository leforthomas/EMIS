package com.geocento.webapps.earthimages.emis.common.share;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class ResourceIcons implements Serializable {

	String treeIconUrl;
	String mapIconUrl;
	int shiftX;
	int shiftY;
	
	public ResourceIcons() {
		// TODO Auto-generated constructor stub
	}

	public ResourceIcons(String treeIconUrl, String mapIconUrl, int shiftX,
                         int shiftY) {
		super();
		this.treeIconUrl = treeIconUrl;
		this.mapIconUrl = mapIconUrl;
		this.shiftX = shiftX;
		this.shiftY = shiftY;
	}

	public ResourceIcons(ResourceIcons icons) {
		this.treeIconUrl = icons.getTreeIconUrl();
		this.mapIconUrl = icons.getMapIconUrl();
		this.shiftX = icons.getShiftX();
		this.shiftY = icons.getShiftY();
	}

	public String getTreeIconUrl() {
		return treeIconUrl;
	}

	public void setTreeIconUrl(String treeIconUrl) {
		this.treeIconUrl = treeIconUrl;
	}

	public String getMapIconUrl() {
		return mapIconUrl;
	}

	public void setMapIconUrl(String mapIconUrl) {
		this.mapIconUrl = mapIconUrl;
	}

	public int getShiftX() {
		return shiftX;
	}

	public void setShiftX(int shiftX) {
		this.shiftX = shiftX;
	}

	public int getShiftY() {
		return shiftY;
	}

	public void setShiftY(int shiftY) {
		this.shiftY = shiftY;
	}

}
