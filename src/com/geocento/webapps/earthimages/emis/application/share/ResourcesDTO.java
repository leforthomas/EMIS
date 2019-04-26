package com.geocento.webapps.earthimages.emis.application.share;

import com.metaaps.webapps.earthimages.extapi.server.domain.Satellite;

import java.io.Serializable;
import java.util.List;

public class ResourcesDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private List<Satellite> satellites;

	public ResourcesDTO() {
	}

    public List<Satellite> getSatellites() {
        return satellites;
    }

    public void setSatellites(List<Satellite> satellites) {
        this.satellites = satellites;
    }

}
