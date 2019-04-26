package com.geocento.webapps.earthimages.emis.common.share.entities;

import com.metaaps.webapps.libraries.client.map.EOBounds;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue("R")
public class AOIRectangle extends AOI implements Serializable {

	// fields to be saved by the project
    @Embedded
	private EOBounds bounds;

	public AOIRectangle() {
	}

    public EOBounds getBounds() {
        return bounds;
    }

    public void setBounds(EOBounds bounds) {
        this.bounds = bounds;
    }
}
