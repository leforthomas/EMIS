package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.List;

/**
 * Created by thomas on 21/03/2017.
 */
public class GeometryUtils {

    static public String getareaStatement = "SELECT ST_Area(ST_GeomFromText('$wkt', 4326)::geography);";

    static public double getAreaostgis(String wkt) throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection connection = null;
        connection = DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1:5432/eineo", "eineo", "eineo");
        ResultSet result = null;
        try {
            result = connection.createStatement().executeQuery(getareaStatement.replace("$wkt", wkt));
            result.next();
            return result.getDouble(1);
        } finally {
            if(result != null) {
                result.close();
            }
            connection.close();
        }
    }

/*
    static public double getArea(EOLatLng[] latLngs) {
        return new AreaFun
    }
*/

    public static String toWKT(List<EOLatLng> points) {
        com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.ensureClosedPolygon(points);
        return "POLYGON((" + EOLatLng.toWKT(points.toArray(new EOLatLng[points.size()])) + "))";
    }

    public static String toWKT(EOLatLng[] points) {
        return toWKT(ListUtil.toList(points));
    }

}
