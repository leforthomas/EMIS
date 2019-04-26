package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;

public class GeometryUtils {

    static public EOBounds getEOBounds(String polygonWKT) throws Exception {
        return EOBounds.getBounds(getCoordinates(polygonWKT));
    }

    private static EOLatLng[] getCoordinates(String polygonWKT) throws Exception {
        return EOLatLng.parseWKT(StringUtils.extract(polygonWKT, "((", "))"));
    }

    public static double getProjectedRatio(EOLatLng startPoint, EOLatLng stopPoint, EOLatLng position) throws Exception {
        double[] positionCoordinates = com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.projectMercator(position);
        double[] startCoordinates = com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.projectMercator(startPoint);
        double[] stopCoordinates = com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.projectMercator(stopPoint);
        double apx = positionCoordinates[0] - startCoordinates[0];
        double apy = positionCoordinates[1] - startCoordinates[1];
        double abx = stopCoordinates[0] - startCoordinates[0];
        double aby = stopCoordinates[1] - startCoordinates[1];

        double ab2 = abx * abx + aby * aby;
        double ap_ab = apx * abx + apy * aby;
        double t = ap_ab / ab2;
        if (t < 0) {
            t = 0;
        } else if (t > 1) {
            t = 1;
        }
        return t;
    }

}
