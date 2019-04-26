package com.geocento.webapps.earthimages.emis.common.share.entities;

import com.metaaps.webapps.libraries.client.map.EOLatLng;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@DiscriminatorValue("C")
public class AOICircle extends AOI implements Serializable {

	// fields to be saved by the project
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="lat", column = @Column(name="center_lat")),
            @AttributeOverride(name="lng", column = @Column(name="center_lng"))
    })
	EOLatLng center;
	double radius;
	
	public AOICircle() {
    }

	public EOLatLng getCenter() {
		return center;
	}
	
	public void setCenter(EOLatLng center) {
		this.center = center;
	}
	
	public double getRadius() {
		return radius;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
}
