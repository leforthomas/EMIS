package com.geocento.webapps.earthimages.emis.application.server.imageapi;

import java.util.List;

/**
 * Created by thomas on 24/05/2016.
 */
public class AoIsImport {

    String message;
    List<AoI> aois;

    public AoIsImport() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AoI> getAois() {
        return aois;
    }

    public void setAois(List<AoI> aois) {
        this.aois = aois;
    }
}
