package com.geocento.webapps.earthimages.emis.application.share;

import com.github.nmorel.gwtjackson.client.ObjectMapper;

public class Extent {

    public static interface ExtentMapper extends ObjectMapper<Extent> {};

    double south;
    double west;
    double north;
    double east;

    public Extent() {
    }

    public double getSouth() {
        return south;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public double getWest() {
        return west;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public double getNorth() {
        return north;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public double getEast() {
        return east;
    }

    public void setEast(double east) {
        this.east = east;
    }
}
