package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOICircle;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOIPolygon;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOIRectangle;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.map.utils.GeometryUtils;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 25/11/2016.
 */
public class AOIUtils {

    static double maxArea = 100000000;

    public static void setMaxArea(double maxArea) {
        AOIUtils.maxArea = maxArea;
    }

    public static double getMaxArea() {
        return maxArea;
    }

    public static void validate(AOI aoi) throws EIException {
        // check area
        double area = getArea(aoi);
        if(area > maxArea) {
            throw new EIException("Shape is too large, area is " + com.metaaps.webapps.libraries.client.widget.util.Utils.formatSurface(com.metaaps.webapps.libraries.client.widget.util.Utils.FORMAT.SQKILOMETERS, 0, area) + ", maximum is " + com.metaaps.webapps.libraries.client.widget.util.Utils.formatSurface(Utils.FORMAT.SQKILOMETERS, 0, maxArea));
        }
        if(aoi instanceof AOIPolygon) {
            List<EOLatLng> points = ((AOIPolygon) aoi).getPoints();
            EOLatLng[] coordinates = points.toArray(new EOLatLng[points.size()]);
            // check for crossed lines as well
            if(EOLatLng.intersects(coordinates)) {
                throw new EIException("Shape contains some crossing lines, please remove the crossing lines");
            }
        }
    }

    public static double getArea(AOI aoi) {
        if(aoi instanceof AOICircle) {
            AOICircle AOICircle = (AOICircle) aoi;
            return com.metaaps.webapps.libraries.client.map.implementation.MapPanel.getPathArea(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.generateCircleCoordinates(AOICircle.getCenter(), AOICircle.getRadius(), true));
        } else if(aoi instanceof AOIRectangle) {
            return com.metaaps.webapps.libraries.client.map.implementation.MapPanel.getPathArea(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.generateNonSphericalRectangleCoordinates(((AOIRectangle) aoi).getBounds(), true));
        } else if(aoi instanceof AOIPolygon) {
            List<EOLatLng> points = ((AOIPolygon) aoi).getPoints();
            EOLatLng[] coordinates = points.toArray(new EOLatLng[points.size()]);
            return com.metaaps.webapps.libraries.client.map.implementation.MapPanel.getPathArea(coordinates);
        }
        return 0;
    }

    static public String getMarkerIconUrl(AOI aoi) {
        if(aoi instanceof AOIRectangle) {
            return "./img/markerRectangle.png";
        } else if(aoi instanceof AOICircle) {
            return "./img/markerCircle.png";
        } else if(aoi instanceof AOIPolygon) {
            return "./img/markerPolygon.png";
        }
        return null;
    }

    public static EOBounds getBounds(AOI aoi) {
        if(aoi instanceof AOICircle) {
            AOICircle AOICircle = (AOICircle) aoi;
            return EOBounds.getBounds(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.generateCircleCoordinates(AOICircle.getCenter(), AOICircle.getRadius(), true));
        } else if(aoi instanceof AOIRectangle) {
            return ((AOIRectangle) aoi).getBounds();
        } else if(aoi instanceof AOIPolygon) {
            AOIPolygon aoiPolygon = (AOIPolygon) aoi;
            List<EOLatLng> points = aoiPolygon.getPoints();
            return EOBounds.getBounds(points.toArray(new EOLatLng[points.size()]));
        }
        return new EOBounds();
    }

    public static String getDefaultName(AOI aoi) {
        String defaultName = "";
        if(aoi instanceof AOICircle) {
            defaultName += "Circle";
        } else if(aoi instanceof AOIRectangle) {
            defaultName += "Rectangle";
        } else if(aoi instanceof AOIPolygon) {
            defaultName += "Polygon";
        }
        return defaultName + "_" + new Date().getTime();
    }

    public static AOI fromWKT(String wkt) throws Exception {
        if(wkt.startsWith("POLYGON((") || wkt.startsWith("MULTIPOLYGON((")) {
            AOIPolygon polygon = new AOIPolygon();
            wkt = StringUtils.extract(wkt, "((", "))");
            EOLatLng[] points = EOLatLng.parseWKT(wkt);
            if(points == null) {
                throw new Exception("Could not parse wkt coordinates");
            }
            polygon.setPoints(ListUtil.toList(points));
            return polygon;
        } else if(wkt.startsWith("POINT(")) {
            AOIRectangle rectangle = new AOIRectangle();
            wkt = StringUtils.extract(wkt, "(", ")");
            EOLatLng[] points = EOLatLng.parseWKT(wkt);
            if(points == null) {
                throw new Exception("Could not parse wkt coordinates");
            }
            EOBounds eoBounds = new EOBounds();
            eoBounds.extend(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.destination(points[0], 500, Math.toRadians(45)));
            eoBounds.extend(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.destination(points[0], 500, Math.toRadians(225)));
            rectangle.setBounds(eoBounds);
            return rectangle;
        }
        throw new Exception("Unsupported geometry type");
    }

    public static String toWKT(AOI aoi) {
        if(aoi instanceof AOICircle) {
            EOLatLng center = ((AOICircle) aoi).getCenter();
            double radius = ((AOICircle) aoi).getRadius();
            return "POLYGON((" + EOLatLng.toWKT(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.generateCircleCoordinates(Math.PI / 180 * center.getLat(), Math.PI / 180 * center.getLng(), radius, true)) + "))";
        } else if(aoi instanceof AOIRectangle) {
            EOBounds bounds = ((AOIRectangle) aoi).getBounds();
            return "POLYGON((" + EOLatLng.toWKT(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.generateNonSphericalRectangleCoordinates(bounds, true)) + "))";
        } else if(aoi instanceof AOIPolygon) {
            List<EOLatLng> points = ((AOIPolygon) aoi).getPoints();
            GeometryUtils.ensureClosedPolygon(points);
            return "POLYGON((" + EOLatLng.toWKT(points.toArray(new EOLatLng[points.size()])) + "))";
        }
        return null;
    }

    static public void sanitizeAoI(AOI aoi) {
        if(aoi.getName() == null) {
            // TODO - make sure the name is unique
            aoi.setName("New AoI");
        }
        aoi.setVisible(true);
        // check if initialised
        if(aoi.getStrokeColor() == null) {
            aoi.setStrokeThickness(2);
            aoi.setStrokeColor("3333CC");
            aoi.setFillColor("33CCCC");
            aoi.setFillOpacity(SettingsHelper.getAoiOpacity() / 100.0);
        }
    }

}
