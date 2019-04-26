package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOICircle;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOIPolygon;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOIRectangle;
import com.geocento.webapps.earthimages.emis.application.client.widgets.MapPanel;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.orders.PolygonSelectionPolicy;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.*;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.map.utils.GeometryUtils;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

/**
 * Created by thomas on 16/06/14.
 */
public class PolicyHelper {

    // at this stage assumes only polygon selection policies are allowed
    public static EOLatLng[] createOptimisedPolygonShape(EOLatLng[] productCoordinates, AOI aoi, PolygonSelectionPolicy policy) throws EIException {
        EOLatLng[] points = null;
        if(aoi instanceof AOIPolygon) {
            points = ((AOIPolygon) aoi).getPoints().toArray(new EOLatLng[((AOIPolygon) aoi).getPoints().size()]);
        } else if(aoi instanceof AOICircle) {
            points = GeometryUtils.generateCircleCoordinates(((AOICircle) aoi).getCenter(), ((AOICircle) aoi).getRadius(), true);
        } else if(aoi instanceof AOIRectangle) {
            points = GeometryUtils.generateNonSphericalRectangleCoordinates(((AOIRectangle) aoi).getBounds(), true);
        }
        if(points == null) {
            throw new EIException("Issue with AOI");
        }
        return clipPolygon(productCoordinates, points);
    }

    public static void validateGeometrySelection(ProductPolicy policy, PRODUCT_SELECTION selectionType, EOLatLng[] selectedGeometry) throws ValidationException {
        switch(selectionType) {
            case frame: {
                //throw new ValidationException("Frame selection not allowed for this policy");
            } break;
            case frameportion: {
                FramePortionSelectionPolicy framePortionPolicy = policy.getFramePortionSelectionPolicy();
                // next check the min area
                double minArea = framePortionPolicy.getMinArea();
                if(minArea > 0) {
                    double selectionArea = getPathArea(selectedGeometry);
                    if(selectionArea < minArea) {
                        throw new ValidationException("The selected area is too small - " + formatSurface(1, selectionArea) + " when minimum required is " + formatSurface(1, minArea));
                    }
                }
            } break;
            case shape: {
                com.geocento.webapps.earthimages.emis.common.client.utils.Utils.printLog("Checking shape selection");
                PolygonSelectionPolicy polygonSelectionPolicy = policy.getPolygonSelectionPolicy();
                if(polygonSelectionPolicy == null) {
                    throw new ValidationException("Polygon selection not allowed for this policy");
                }
                // first make sure that the selection polygon is valid
                if(EOLatLng.intersects(selectedGeometry)) {
                    com.geocento.webapps.earthimages.emis.common.client.utils.Utils.printLog("Geometry intersects");
                    throw new ValidationException("Selection is a self intersecting polygon.");
                }
                // next check the number of points
                double maxPoints = polygonSelectionPolicy.getMaxNumberOfPoints();
                if(maxPoints > 0) {
                    int selectionPoints = selectedGeometry.length;
                    if(selectionPoints > maxPoints) {
                        throw new ValidationException("The selected shape has too many points - " + selectionPoints + " when the maximum allowed is " + maxPoints);
                    }
                }
                // next check the min area
                double minArea = polygonSelectionPolicy.getMinArea();
                if(minArea > 0) {
                    double selectionArea = getPathArea(selectedGeometry);
                    if(selectionArea < minArea) {
                        com.geocento.webapps.earthimages.emis.common.client.utils.Utils.printLog("Geometry area is too small");
                        throw new ValidationException("The selected area is too small - " + formatSurface(1, selectionArea) + " when minimum required is " + formatSurface(1, minArea));
                    }
                }
                double minWidth = polygonSelectionPolicy.getMinAcrossWidth() / 1000.0;
                if(minWidth != 0) {
                    if(!validateMinAcrossWidth(selectedGeometry, minWidth)) {
                        throw new ValidationException("This policy requires a minimum width of " + minWidth + " kilometers");
                    }
                }
                com.geocento.webapps.earthimages.emis.common.client.utils.Utils.printLog("Geometry is fine");
            } break;
        }
    }

    private static EOLatLng[] bufferPolyline(EOLatLng[] polylineCoordinates, double max) {
        return MapPanel.bufferPolyline(polylineCoordinates, max);
    }

    private static EOLatLng[] clipPolygon(EOLatLng[] firstCoordinates, EOLatLng[] secondCoordinates) {
        return MapPanel.clipPolygon(firstCoordinates, secondCoordinates);
    }

    private static String formatSurface(int precision, double area) {
        return Utils.formatSurface(Utils.FORMAT.SQKILOMETERS, precision, area);
    }

    private static double getPathArea(EOLatLng[] geometry) {
        return MapPanel.getPathArea(geometry);
    }

    private static String formatDistance(double distance) {
        return Utils.formatDistance(distance);
    }
    
    public static boolean validateSelection(PRODUCT_SELECTION selectionType, ProductPolicy specificPolicy) {
/*
        return (selectionType == Policy.SELECTION.frame && specificPolicy.getFrameSelectionPolicy() != null) ||
                (selectionType == Policy.SELECTION.frameportion && specificPolicy.getFramePortionSelectionPolicy() != null) ||
                (selectionType == Policy.SELECTION.shape && specificPolicy.getShapeSelectionPolicy() != null && specificPolicy.getShapeSelectionPolicy().getPolygonConstraintPolicy() != null)
        ;
*/
        return true;
    }

    public static String getDeliveryTime(Product product) {
        if(product.getSatelliteName().toLowerCase().startsWith("planet")) {
            return "10 mns";
        } else if(product.getSatelliteName().toLowerCase().startsWith("senti") || product.getSatelliteName().toLowerCase().startsWith("landsat")) {
            return "15 mns";
        } else {
            return "3 days";
        }
    }

    public static EOLatLng[] createOptimisedFrameSelection(EOLatLng[] frameCoordinates, AOI aoI, FramePortionSelectionPolicy framePortionSelectionPolicy) throws Exception {
        // start by finding corners for the product coordinates
        EOLatLng[] corners = null;
        try {
            corners = GeometryUtils.getCornersFromAngles(frameCoordinates);
        } catch(Exception e) {
            corners = GeometryUtils.getCorners(frameCoordinates);
        }
        EOLatLng frameTopLeft = corners[0];
        EOLatLng frameTopRight = corners[1];
        EOLatLng frameBottomRight = corners[2];
        EOLatLng frameBottomLeft = corners[3];

        // now look for the north most and south most point for the AoI
        EOLatLng[] aoiCoordinates = ProductHelper.getCoordinates(AOIUtils.toWKT(aoI));
        double minRatio = 1;
        double maxRatio = 0;
        for(EOLatLng eoLatLng : aoiCoordinates) {
            // project to find min and max ratio
            double ratio = com.geocento.webapps.earthimages.emis.application.client.utils.GeometryUtils.getProjectedRatio(frameBottomLeft, frameTopLeft, eoLatLng);
            minRatio = Math.min(minRatio, ratio);
            maxRatio = Math.max(maxRatio, ratio);
        }
        minRatio = Math.max(minRatio, 0);
        maxRatio = Math.min(maxRatio, 1);

        return new EOLatLng[] {
                GeometryUtils.interpolate(frameBottomLeft, frameTopLeft, minRatio),
                GeometryUtils.interpolate(frameBottomLeft, frameTopLeft, maxRatio),
                GeometryUtils.interpolate(frameBottomRight, frameTopRight, maxRatio),
                GeometryUtils.interpolate(frameBottomRight, frameTopRight, minRatio)
        };
    }

    public static EULADocumentDTO getLicensingEULADocument(LicensingPolicy licensingPolicy, String value) {
        // get options
        // basic checks first
        if(licensingPolicy == null || value == null || licensingPolicy.getParameter().getLicenseOptions() == null) {
            return null;
        }
        // now look for the matching value
        return ListUtil.findValue(licensingPolicy.getParameter().getLicenseOptions(), option -> option.getOption().contentEquals(value)).getEulaDocument();
    }

    public static String getLicensingEULAUrl(EULADocumentDTO eulaDocument) {
        return getLicensingEULAUrl(eulaDocument.getId());
    }

    public static String getLicensingEULAUrl(Long eulaDocumentId) {
        return "./api/license/eula/download/" + eulaDocumentId;
    }

    public static boolean validateMinAcrossWidth(EOLatLng[] coordinates, double minWidthKilometers) {
        double[][] values = new double[coordinates.length + 1][2];
        int index = 0;
        for (EOLatLng eoLatLng : coordinates) {
            values[index][0] = eoLatLng.getLng();
            values[index][1] = eoLatLng.getLat();
            index++;
        }
        values[index] = values[0];
        return validateMinAcrossWidth(values, minWidthKilometers);
    }

    private native static boolean validateMinAcrossWidth(double[][] coordinates, double minWidthKilometers) /*-{
        var turf = $wnd['turf'];
        coordinates = [coordinates];
        var inputPolygon = turf.polygon(coordinates);
        // now buffer
        var bufferSize = minWidthKilometers / 2;
        var skeleton = turf.buffer(inputPolygon, -1 * bufferSize, {units: 'kilometers'});
        // check the polygon hasn't been broken in sub polygons
        var polygonValid = skeleton && skeleton.geometry && skeleton.geometry.coordinates.length == 1;
        if(polygonValid) {
            var newPolygon = turf.buffer(skeleton, bufferSize * 1.1, {units: 'kilometers'});
            // check the new polygon contains the old one
            // we have round edges now so we use the half sides instead
            var index, points = [];
            for(index = 0; index < coordinates[0].length - 1; index++) {
                var point = turf.midpoint(turf.point(coordinates[0][index]), turf.point(coordinates[0][index + 1]));
                points.push(point.geometry.coordinates);
            }
            var validPoints = turf.pointsWithinPolygon(turf.points(points), newPolygon);
            polygonValid = validPoints.features.length == points.length;
        }
        return polygonValid;
    }-*/;

}
