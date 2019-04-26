package com.geocento.webapps.earthimages.emis.common.share.entities;

import com.metaaps.webapps.libraries.client.map.EOLatLng;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Observation Decorator for the project
 * Provides methods for map rendering
 * 
 */
@Entity
@DiscriminatorValue("P")
public class AOIPolygon extends AOI implements Serializable {

	// fields to be saved by the project
    @Lob
	private List<EOLatLng> points = new ArrayList<EOLatLng>();
	
	// no argument constructor needed otherwise Serialization won't work
	public AOIPolygon() {
	}

    public List<EOLatLng> getPoints() {
        return points;
    }

    public void setPoints(List<EOLatLng> points) {
        this.points = points;
    }
}
