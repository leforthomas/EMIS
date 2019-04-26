package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.Feature;
import com.geocento.webapps.earthimages.emis.common.share.LAYER_TYPE;
import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.UserLayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.client.utils.ProductDisplay;
import com.geocento.webapps.earthimages.emis.application.client.utils.WMSCapabilities;
import com.geocento.webapps.earthimages.emis.application.share.ProductMetadataDTO;
import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.metaaps.webapps.earthimages.extapi.server.domain.TYPE;
import com.metaaps.webapps.libraries.client.map.*;
import com.metaaps.webapps.libraries.client.map.utils.GeometryUtils;
import com.metaaps.webapps.libraries.client.widget.ToggledIconAnchor;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Util;

import java.util.*;

/**
 * Created by Thomas on 14/01/2015.
 */
public class MapPanel extends com.metaaps.webapps.libraries.client.map.implementation.MapPanel {

    static public interface AoIChangedHandler {

        void notifyUpdate();

        void notifyEdit();
    }

    static public interface Presenter {
        void onProductClicked(ProductDisplay product, EOLatLng eoLatLng);
    }

    static private class StripRendering {
        UniPolyline stripContour;
    }

    static private class ProductRendering {
        UniPolygon polygon;
        UniWMSLayer overlay;
        List<ProductOrderRendering> productOrderRendering;
    }

    protected class ProductOrderRendering {
        UniPolyline outline;
        List<UniWMSLayer> overlays;
    }

    private static DateTimeFormat timeWMSFMT = DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    static private class WMSTimeRendering {
        Date time;
        UniWMSLayer uniWMSLayer;
    }

    private static String outlinedColor = "#0000ff";
    private static String highlightedColor = "#ffffff";
    private static String productArchiveColor = "#cc0000";
    private static String productFutureColor = "#cccc00";
    private static String selectedCartColor = "#33CC33";
    private static double reducedDisplayOpacity = 0.3;
    private static int selectedCartThickness = 2;
    private static float notSelectedOpacity = 1;
    private static int notSelectedThickness = 1;
    private static int highlightedThickness = 3;
    private static String stripContourColor = productArchiveColor; //"#79c8cf";
    private static int stripHighlightedThickness = 2;
    private double stripSceneOpacity = 0.3;

    private HandlerRegistration featureInfoHandler = null;

    private HashMap<ProductDisplay, ProductRendering> productsRendering = new HashMap<ProductDisplay, ProductRendering>();

    private HashMap<String, StripRendering> stripsRendering = new HashMap<String, StripRendering>();

    protected HashMap<UserLayerDTO, Object> layersRendering = new HashMap<UserLayerDTO, Object>();

    private double productOpacity;
    private double overlaysOpacity;

    private ProductDisplay currentlyHighlightedProduct;

    private ProductDisplay currentlyOutlinedProduct;

    private boolean reducedDisplay;

    private Presenter presenter;

    static public interface ObservationChangedHandler {

        void notifyUpdate();

        void notifyEdit();
    }

    static protected class FeatureRendering {
        Object rendering;
        UniPolyline polyline;
        UniMarker marker;

        public ObservationChangedHandler handler;

        public Object getRendering() {
            return rendering;
        }

        public UniPolyline getPolyline() {
            return polyline;
        }

        public UniMarker getMarker() {
            return marker;
        }
    }

    protected HashMap<AOI, FeatureRendering> featuresRendering = new HashMap<AOI, FeatureRendering>();

    public MapPanel() {
        clearMapLibraries();
        addMapLibrary("Google Maps", "googleMapsV3", new String[]{"./js/mapsv0.6.js?3", "./js/maps.css?1"});
        // check for Cesium support
        if(isCesiumSupported()) {
            addMapLibrary("Cesium Globe", "CesiumMaps", new String[]{"./js/Cesium/Cesium.js", "./js/cesiumv0.6.js?3", "./js/Cesium/Widgets/CesiumWidget/CesiumWidget.css", "./js/cesium.css"});
        }
    }

    public void drawNewFeature(AOI feature, final AsyncCallback<AOI> callback) {
        if(feature instanceof AOICircle) {
            final AOICircle circleFeature = (AOICircle) feature;
            createCircle("#" + circleFeature.getStrokeColor(), circleFeature.getStrokeThickness(), 1.0, "#" + circleFeature.getFillColor(), circleFeature.getFillOpacity(), false, true, null, new CircleCompletedHandler() {

                @Override
                public void onCircleCompleted(EOLatLng center, double radius) {
                    circleFeature.setCenter(center);
                    circleFeature.setRadius(radius);
                    callback.onSuccess(circleFeature);
                }
            });
        } else if(feature instanceof AOIRectangle) {
            final AOIRectangle rectangleFeature = (AOIRectangle) feature;
            createRectangle("#" + rectangleFeature.getStrokeColor(), rectangleFeature.getStrokeThickness(), 1.0, "#" + rectangleFeature.getFillColor(), rectangleFeature.getFillOpacity(), false, true, null, new RectangleCompletedHandler() {

                @Override
                public void onRectangleCompleted(EOBounds bounds) {
                    rectangleFeature.setBounds(bounds);
                    callback.onSuccess(rectangleFeature);
                }
            });
        } else if(feature instanceof AOIPolygon) {
            final AOIPolygon polygonFeature = (AOIPolygon) feature;
            createPolygon("#" + polygonFeature.getStrokeColor(), polygonFeature.getStrokeThickness(), 1.0, "#" + polygonFeature.getFillColor(), polygonFeature.getFillOpacity(), false, true, null, new PolygonCompletedHandler() {

                @Override
                public void onPolygonCompleted(JsArrayNumber points) {
                    polygonFeature.setPoints(toEOLatLngList(points));
                    callback.onSuccess(polygonFeature);
                }
            });
        }
    }

    private List<EOLatLng> toEOLatLngList(JsArrayNumber points) {
        return ListUtil.toList(toEOLatLngArray(points));
    }

    public void addEditableFeature(AOI feature, final ObservationChangedHandler handler) {
        if(featuresRendering.containsKey(feature)) {
            return;
        }
        if(map == null || feature == null) {
            return;
        }
        final FeatureRendering featureRendering = new FeatureRendering();
        if(feature instanceof AOICircle) {
            final AOICircle circleFeature = (AOICircle) feature;
            // add draggable marker for the center
            final UniMarker marker = addEditableMarker(new MarkerCompletedHandler() {

                @Override
                public void onMarkerCompleted(EOLatLng position) {
                    ((UniCircle) featureRendering.rendering).setCenter(position);
                    circleFeature.setCenter(position);
                    // notify of update
                    handler.notifyUpdate();
                }
            }, circleFeature.getCenter(), AOIUtils.getMarkerIconUrl(feature), 16, 32);
            marker.addEventHandler("drag", new MarkerEventHandler() {

                @Override
                public void onEvent(UniMarker marker) {
                    ((UniCircle) featureRendering.rendering).setCenter(marker.getEOLatLng());
                }
            });
            marker.setTitle(circleFeature.getName() + " - Drag me to move the Circle, click to edit.");
            marker.addClickHandler(new com.metaaps.webapps.libraries.client.map.ClickHandler() {

                @Override
                public void onMapClick(double lat, double lng) {
                    handler.notifyEdit();
                }
            });
            featureRendering.rendering = addEditableCircle(new CircleCompletedHandler() {
                @Override
                public void onCircleCompleted(EOLatLng center, double radius) {
                    circleFeature.setCenter(center);
                    circleFeature.setRadius(radius);
                    // update the center marker
                    marker.setPosition(circleFeature.getCenter());
                    // notify of update
                    handler.notifyUpdate();
                }
            }, circleFeature.getCenter(), circleFeature.getRadius(), "#" + circleFeature.getStrokeColor(), circleFeature.getStrokeThickness(), 0.9, "#" + circleFeature.getFillColor(), circleFeature.getFillOpacity(), true, true);
            ((UniCircle) featureRendering.rendering).setOnTop();
            featureRendering.marker = marker;
        } else if(feature instanceof AOIRectangle) {
            final AOIRectangle rectangleFeature = (AOIRectangle) feature;
            // add Observation Marker
            final UniMarker marker = addEditableMarker(new MarkerCompletedHandler() {

                @Override
                public void onMarkerCompleted(EOLatLng position) {
                    rectangleFeature.setBounds(((UniRectangle) featureRendering.rendering).getBounds());
                    // notify of update
                    handler.notifyUpdate();
                }
            }, new EOLatLng(0, 0), AOIUtils.getMarkerIconUrl(feature), 16, 32);
            marker.addEventHandler("drag", new MarkerEventHandler() {

                @Override
                public void onEvent(UniMarker marker) {
                    if (!(((UniRectangle) featureRendering.rendering).setCenter(marker.getEOLatLng()))) {
                        marker.setPosition(((UniRectangle) featureRendering.rendering).getCenter());
                    }
                }
            });
            marker.setTitle(rectangleFeature.getName() + " - Drag me to move the Rectangle, click to edit.");
            marker.addClickHandler(new com.metaaps.webapps.libraries.client.map.ClickHandler() {

                @Override
                public void onMapClick(double lat, double lng) {
                    handler.notifyEdit();
                }
            });
            featureRendering.rendering = addEditableRectangle(new RectangleCompletedHandler() {

                @Override
                public void onRectangleCompleted(EOBounds bounds) {
                    rectangleFeature.setBounds(bounds);
                    // update the center marker
                    marker.setPosition(((UniRectangle) featureRendering.rendering).getCenter());
                    // notify of update
                    handler.notifyUpdate();
                }

            }, rectangleFeature.getBounds(), "#" + rectangleFeature.getStrokeColor(), rectangleFeature.getStrokeThickness(), 0.9, "#" + rectangleFeature.getFillColor(), rectangleFeature.getFillOpacity(), true, true);
            ((UniRectangle) featureRendering.rendering).setOnTop();
            marker.setPosition(((UniRectangle) featureRendering.rendering).getCenter());
            featureRendering.marker = marker;
        } else if(feature instanceof AOIPolygon) {
            final AOIPolygon polygonFeature = (AOIPolygon) feature;
            // add Observation Marker
            final UniMarker marker = addEditableMarker(new MarkerCompletedHandler() {

                @Override
                public void onMarkerCompleted(EOLatLng position) {
                    List<EOLatLng> positions = ((UniPolygon) featureRendering.rendering).getPositions();
                    polygonFeature.setPoints(positions);
                    // notify of update
                    handler.notifyUpdate();
                }
            }, new EOLatLng(0, 0), AOIUtils.getMarkerIconUrl(feature), 16, 32);
            marker.addEventHandler("drag", new MarkerEventHandler() {

                @Override
                public void onEvent(UniMarker marker) {
                    // shift all polygon by the new position
                    ((UniPolygon) featureRendering.rendering).setCenter(marker.getEOLatLng());
                }
            });
            marker.setTitle(polygonFeature.getName() + " Drag me to move the points - click to edit.");
            marker.addClickHandler(new com.metaaps.webapps.libraries.client.map.ClickHandler() {

                @Override
                public void onMapClick(double lat, double lng) {
                    handler.notifyEdit();
                }
            });
            List<EOLatLng> positions = polygonFeature.getPoints();
            featureRendering.rendering = addEditablePolygon(new PolygonCompletedHandler() {

                @Override
                public void onPolygonCompleted(JsArrayNumber points) {
                    // update points
                    polygonFeature.setPoints(toEOLatLngList(points));
                    // update the center marker
                    marker.setPosition(((UniPolyline) featureRendering.rendering).getCenter());
                    // notify of update
                    handler.notifyUpdate();
                }
            }, positions.toArray(new EOLatLng[positions.size()]), "#" + polygonFeature.getStrokeColor(), polygonFeature.getStrokeThickness(), 0.9, "#" + polygonFeature.getFillColor(), polygonFeature.getFillOpacity(), true, true);
            ((UniPolygon) featureRendering.rendering).setOnTop();
            marker.setPosition(((UniPolygon) featureRendering.rendering).getCenter());
            featureRendering.marker = marker;
        }
        featureRendering.handler = handler;
        featuresRendering.put(feature, featureRendering);
    }

    public void addFeature(AOI feature) {
        if(featuresRendering.containsKey(feature)) {
            return;
        }
        if(map == null || feature == null) {
            return;
        }
        sanitizeFeature(feature);
        FeatureRendering featureRendering = new FeatureRendering();
        if(feature instanceof AOICircle) {
            AOICircle circleFeature = (AOICircle) feature;
            featureRendering.rendering = addCircle(circleFeature.getCenter(), circleFeature.getRadius(), "#" + circleFeature.getStrokeColor(), circleFeature.getStrokeThickness(), 0.9, "#" + circleFeature.getFillColor(), circleFeature.getFillOpacity());
            ((UniCircle) featureRendering.rendering).setTooltip("Feature " + circleFeature.getName());
            EOLatLng[] points = GeometryUtils.generateCircleCoordinates(circleFeature.getCenter(), circleFeature.getRadius(), true);
            featureRendering.polyline = addPolyline(points, "#" + circleFeature.getStrokeColor(), circleFeature.getStrokeThickness(), 0.9, false, true);
        } else if(feature instanceof AOIRectangle) {
            AOIRectangle rectangleFeature = (AOIRectangle) feature;
            featureRendering.rendering = addRectangle(rectangleFeature.getBounds(), "#" + rectangleFeature.getStrokeColor(), rectangleFeature.getStrokeThickness(), 0.9, "#" + rectangleFeature.getFillColor(), rectangleFeature.getFillOpacity(), false, true);
            ((UniRectangle) featureRendering.rendering).setTooltip("Feature " + rectangleFeature.getName());
            featureRendering.polyline = addPolyline(GeometryUtils.generateNonSphericalRectangleCoordinates(rectangleFeature.getBounds(), true), "#" + rectangleFeature.getStrokeColor(), rectangleFeature.getStrokeThickness(), 0.9, false, false);
        } else if(feature instanceof AOIPolygon) {
            AOIPolygon polygonFeature = (AOIPolygon) feature;
            List<EOLatLng> positions = polygonFeature.getPoints();
            featureRendering.rendering = addPolygon(positions.toArray(new EOLatLng[positions.size()]), "#" + polygonFeature.getStrokeColor(), polygonFeature.getStrokeThickness(), 0.9, "#" + polygonFeature.getFillColor(), polygonFeature.getFillOpacity(), false, true);
            ((UniPolygon) featureRendering.rendering).setTooltip("Feature " + polygonFeature.getName());
            positions.add(positions.get(0));
            featureRendering.polyline = addPolyline(positions.toArray(new EOLatLng[positions.size()]), "#" + polygonFeature.getStrokeColor(), polygonFeature.getStrokeThickness(), 0.9, false, true);
        }
        featuresRendering.put(feature, featureRendering);
    }

    private void sanitizeFeature(AOI feature) {
        if(feature.getFillColor() == null) {
            feature.setFillColor("aa3333");
        }
        if(feature.getStrokeColor() == null) {
            feature.setStrokeColor("3333aa");
        }
    }

    public void addFeatures(List<AOI> observations) {
        for(AOI observationImplementation : observations) {
            addFeature(observationImplementation);
        }
    }

    public void updateFeatures() {
        // update the features
        for(AOI observation : featuresRendering.keySet()) {
            updateFeature(observation);
        }
    }

    public void updateFeature(AOI aoi) {
        FeatureRendering featureRendering = featuresRendering.get(aoi);
        if(aoi == null) {
            return;
        }
        EOLatLng center = null;
        if(aoi instanceof AOICircle) {
            AOICircle circlefeature = (AOICircle) aoi;
            UniCircle circle = (UniCircle) featureRendering.rendering;
            circle.setCenter(circlefeature.getCenter());
            circle.setRadius(circlefeature.getRadius());
            circle.setVisible(aoi.isVisible());
            circle.setStrokeStyle("#" + aoi.getStrokeColor(), (float) 0.9, aoi.getStrokeThickness());
            circle.setFillColor("#" + aoi.getFillColor());
            circle.setFillOpacity(aoi.getFillOpacity());
            center = ((UniCircle) featureRendering.rendering).getCenter();
        } else if(aoi instanceof AOIRectangle) {
            AOIRectangle rectangleFeature = (AOIRectangle) aoi;
            UniRectangle rectangle = (UniRectangle) featureRendering.rendering;
            rectangle.setBounds(rectangleFeature.getBounds());
            rectangle.setVisible(aoi.isVisible());
            rectangle.setStrokeStyle("#" + aoi.getStrokeColor(), (float) 0.9, aoi.getStrokeThickness());
            rectangle.setFillColor("#" + aoi.getFillColor());
            rectangle.setFillOpacity(aoi.getFillOpacity());
            center = rectangle.getCenter();
        } else if(aoi instanceof AOIPolygon) {
            AOIPolygon polygonFeature = (AOIPolygon) aoi;
            UniPolygon polygon = (UniPolygon) featureRendering.rendering;
            List<EOLatLng> points = polygonFeature.getPoints();
            polygon.updatePositions(points.toArray(new EOLatLng[points.size()]));
            polygon.setVisible(aoi.isVisible());
            polygon.setStrokeStyle("#" + aoi.getStrokeColor(), (float) 0.9, aoi.getStrokeThickness());
            polygon.setFillColor("#" + aoi.getFillColor());
            polygon.setFillOpacity(aoi.getFillOpacity());
        }
        UniPolyline polyline = featureRendering.polyline;
        if(polyline != null) {
            polyline.setVisible(aoi.isVisible());
            polyline.setOnTop();
        }
        UniMarker marker = featureRendering.marker;
        if(marker != null) {
            marker.setVisible(aoi.isVisible());
            if(center != null) {
                marker.setPosition(center);
            }
        }
    }

    public void removeFeature(AOI feature) {
        clearFeature(feature);
        featuresRendering.remove(feature);
    }

    public void clearFeatures() {
        for(AOI feature : featuresRendering.keySet()) {
            clearFeature(feature);
        }
        featuresRendering.clear();
    }

    protected void clearFeature(AOI feature) {
        FeatureRendering rendering = featuresRendering.get(feature);
        if(rendering != null) {
            if(rendering.rendering != null) {
                removeOverlay(rendering.rendering);
            }
            if(rendering.polyline != null) {
                removeOverlay(rendering.polyline);
            }
            if(rendering.marker != null) {
                removeOverlay(rendering.marker);
            }
        }
    }

    @Override
    protected void postInit() {
        super.postInit();
        map.addZoomOnShift(new RectangleCompletedHandler() {

            @Override
            public void onRectangleCompleted(EOBounds bounds) {
                setEOBounds(bounds);
            }
        });
    }

    public void fitBoundsToContent() {
        EOBounds bounds = new EOBounds();
        for(ProductDisplay product : productsRendering.keySet()) {
            ProductRendering productRendering = productsRendering.get(product);
            if(product.isVisible() && productRendering.polygon != null) {
                bounds.extend(productRendering.polygon.getEOBounds());
            }
        }
        for(AOI aoi : featuresRendering.keySet()) {
            if(aoi.isVisible()) {
                bounds.extend(AOIUtils.getBounds(aoi));
            }
        }
        setEOBounds(bounds);
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void sortProductsDisplay() {
        // order products by bounding box
        List<ProductDisplay> products = new ArrayList<ProductDisplay>(productsRendering.keySet());
        // sort from top to bottom
        Collections.sort(products, new Comparator<ProductDisplay>() {
            @Override
            public int compare(ProductDisplay p1, ProductDisplay p2) {
                return p1.getProduct().getArea() < p2.getProduct().getArea() ? 1 : -1;
            }
        });
        // now put on top by order of size, smallest at the top
        for(ProductDisplay product : products) {
            productsRendering.get(product).polygon.setOnTop();
        }
    }

    public void addProduct(final ProductDisplay product) {
        if(map == null || product == null) {
            return;
        }
        if(productsRendering.containsKey(product)) {
            return;
        }
        EOLatLng[] coordinates = product.getCoordinates();
        if(coordinates == null) {
            return;
        }
        // add polygon with default values
        UniPolygon polygon = addPolygon(coordinates, "#ffffff", 1, 1.0, "#ff0000", 0.5, false, true);
        polygon.setOnTop();
        polygon.setTooltip("ProductDisplay from " + product.getProduct().getSatelliteName() + " " + product.getProduct().getInstrumentName() + " on " + DateUtil.displayUTCDate(product.getProduct().getStart()));
        polygon.addClickHandler(new ClickHandler() {

            @Override
            public void onMapClick(double lat, double lng) {
                if (!isReducedDisplay() && presenter != null) {
                    presenter.onProductClicked(product, new EOLatLng(lat, lng));
                }
            }
        });
        ProductRendering productRendering = new ProductRendering();
        productRendering.polygon = polygon;
        productsRendering.put(product, productRendering);

        // add strip if the product has a strip
        List<EOLatLng> stripContour = product.getStripContour();
        if(stripContour != null) {
            String stripId = product.getProduct().getStrip();
            if(!stripsRendering.containsKey(stripId)) {
                UniPolyline stripPolyline = addPolyline(stripContour.toArray(new EOLatLng[stripContour.size()]), stripContourColor, 1, 0.7, false, true);
                StripRendering stripRendering = new StripRendering();
                stripRendering.stripContour = stripPolyline;
                stripsRendering.put(stripId, stripRendering);
                stripPolyline.setVisible(false);
            }
        }
        // add product orders if product has product orders
        if(product.getProductOrders() != null && product.getProductOrders().size() > 0) {
            productRendering.productOrderRendering = new ArrayList<ProductOrderRendering>();
            for(ProductOrderDTO productOrderDTO : product.getProductOrders()) {
                EOLatLng[] productOrderCoordinates = productOrderDTO.getCoordinates();
                if(productOrderCoordinates == null) {
                    return;
                }
                // add polygon with default values
                UniPolyline polyline = addPolyline(GeometryUtils.closePath(productOrderCoordinates), "#ffffff", 1, 1.0, false, true);
                polyline.setOnTop();
                polyline.setTooltip("Product from " + productOrderDTO.getDescription());
                ProductOrderRendering productOrderRendering = new ProductOrderRendering();
                productOrderRendering.outline = polyline;
                ArrayList<ProductMetadataDTO> publishedProducts = productOrderDTO.getPublishedProducts();
                // TODO - check the layer name is not null, etc...
                if(productOrderDTO.getStatus() == PRODUCTORDER_STATUS.Completed && publishedProducts != null) {
                    List<UniWMSLayer> overlays = new ArrayList<UniWMSLayer>();
                    for(ProductMetadataDTO productMetadataDTO : publishedProducts) {
                        UniWMSLayer overlay = addWMSLayer(productOrderDTO.getProductWMSServiceURL(),
                                productMetadataDTO.getPublishUri(),
                                "1.1.1", null, null, "EPSG:3857", EOBounds.getBounds(productOrderCoordinates));
                        //overlay.setZIndex(productOrderRendering.zIndex * 10 + overlays.size());
                        overlays.add(overlay);
                    }
                    productOrderRendering.overlays = overlays;
                }
                productRendering.productOrderRendering.add(productOrderRendering);
            }
        }

        updateProductDisplay(product);
    }

    public void updateProductDisplay(ProductDisplay product) {
        ProductRendering rendering = productsRendering.get(product);
        boolean outlined = product == currentlyOutlinedProduct;
        // check if it has a strip
        String stripId = product.getProduct().getStrip();
        boolean isStrip = stripsRendering.containsKey(stripId);
        if(rendering != null) {
            UniPolygon polygon = rendering.polygon;
            UniWMSLayer overlay = rendering.overlay;
            // first check product is visible
            if(!product.isVisible() && !outlined) {
                polygon.setVisible(false);
                if(overlay != null) {
                    overlay.setVisible(false);
                }
                if(isStrip) {
                    stripsRendering.get(stripId).stripContour.setVisible(false);
                }
                showProductOrderRendering(rendering.productOrderRendering, false);
                return;
            } else {
                polygon.setVisible(true);
                boolean highlighted = product == currentlyHighlightedProduct;
                boolean isProductInCart = false; //CartHelper.isProductInCart(product.getProduct());
                double strokeOpacity = highlighted || outlined ? 1 :
                        isStrip ? stripSceneOpacity :
                                (reducedDisplay ? reducedDisplayOpacity :
                                        notSelectedOpacity);
                int strokeThickness = (outlined || highlighted) ? highlightedThickness : (isProductInCart ? selectedCartThickness : notSelectedThickness);
                String strokeColor = isProductInCart ? selectedCartColor :
                        outlined ? outlinedColor :
                        highlighted ? highlightedColor :
//                                com.geocento.webapps.earthimages.emis.application.client.utils.ProductHelper.getProductColor(product.getProduct())
                                product.getProduct().getType() == TYPE.ARCHIVE ? productArchiveColor : productFutureColor
                        ;
                polygon.setFillColor("#8800AA");
                polygon.setFillOpacity(product.isDisplayImage() ? 0 : (reducedDisplay ? 0 : productOpacity));
                polygon.setStrokeStyle(strokeColor, strokeOpacity, strokeThickness);
                if(product.isDisplayImage()) { // && (!reducedDisplay || highlighted)) {
                    if(overlay == null) {
                        overlay = addWMSLayer(product.getProduct().getQl(), "quicklook", "1.1.1", null, null, "EPSG:3857", product.getBounds());
                        rendering.overlay = overlay;
                    }
                    overlay.setOpacity(overlaysOpacity);
                    overlay.setVisible(true);
                } else {
                    if(overlay != null) {
                        overlay.setVisible(false);
                    }
                }
                if(isStrip) {
                    UniPolyline stripContour = stripsRendering.get(stripId).stripContour;
                    stripContour.setVisible(true);
                    strokeOpacity = highlighted || outlined ? 1.0 :
                            reducedDisplay ? reducedDisplayOpacity :
                                    notSelectedOpacity;
                    strokeColor = stripContourColor;
                    strokeThickness = outlined ? stripHighlightedThickness : notSelectedThickness;
                    stripContour.setStrokeStyle(strokeColor, strokeOpacity, strokeThickness);
                }
                showProductOrderRendering(rendering.productOrderRendering, product.isShowProductOrders());
                if(highlighted) {
                    polygon.setOnTop();
                }
            }
        }
    }

    private void showProductOrderRendering(List<ProductOrderRendering> productOrderRenderings, boolean showProductOrders) {
        if(productOrderRenderings != null && productOrderRenderings.size() > 0) {
            for(ProductOrderRendering productOrderRendering : productOrderRenderings) {
                productOrderRendering.outline.setVisible(showProductOrders);
                if(productOrderRendering.overlays != null) {
                    for(UniWMSLayer productOrderOverlay : productOrderRendering.overlays) {
                        productOrderOverlay.setVisible(showProductOrders);
                    }
                }
            }
        }
    }

    public void updateDisplay() {
        updateFeatures();
        for(ProductDisplay product : productsRendering.keySet()) {
            updateProductDisplay(product);
        }
    }

    public void removeProduct(ProductDisplay product) {
        if(productsRendering.containsKey(product)) {
            removeProductRendering(productsRendering.get(product));
            productsRendering.remove(product);
        }
    }

    private void removeProductRendering(ProductRendering productRendering) {
        if(productRendering.polygon != null) {
            removeOverlay(productRendering.polygon);
        }
        if(productRendering.overlay != null) {
            productRendering.overlay.setVisible(false);
        }
    }

    private void removeStripRendering(StripRendering stripRendering) {
        if(stripRendering.stripContour != null) {
            removeOverlay(stripRendering.stripContour);
        }
    }

    public void clearProducts() {
        for(ProductRendering productRendering : productsRendering.values()) {
            removeProductRendering(productRendering);
        }
        productsRendering.clear();

        for(StripRendering stripRendering : stripsRendering.values()) {
            removeStripRendering(stripRendering);
        }
        stripsRendering.clear();
    }

    // only one highlighted product at a time
    public void highlightProduct(ProductDisplay product) {
        ProductDisplay oldHighlightedProduct = currentlyHighlightedProduct;
        currentlyHighlightedProduct = product;
        // update the map
        if(oldHighlightedProduct != null) {
            // change currently the highlighted product
            updateProductDisplay(oldHighlightedProduct);
        }
        for(AOI aoi : featuresRendering.keySet()) {
            if(aoi.isVisible()) {
                UniPolyline polyline = featuresRendering.get(aoi).getPolyline();
                if(polyline != null) {
                    polyline.setOnTop();
                }
            }
        }
        if(product != null) {
            updateProductDisplay(product);
            // and then the selected product above
            productsRendering.get(product).polygon.setOnTop();
        }
    }

    public void centerOnProduct(ProductDisplay product) {
        ProductRendering rendering = productsRendering.get(product);
        if(rendering != null && rendering.polygon != null) {
            setCenter(rendering.polygon.getCenter());
        }
    }

    public double getProductOpacity() {
        return productOpacity;
    }

    public void setProductOpacity(double productOpacity) {
        this.productOpacity = productOpacity;
    }

    public double getOverlaysOpacity() {
        return overlaysOpacity;
    }

    public void setOverlaysOpacity(double overlaysOpacity) {
        this.overlaysOpacity = overlaysOpacity;
    }

    public void setReducedDisplay(boolean reducedDisplay) {
        this.reducedDisplay = reducedDisplay;
    }

    public boolean isReducedDisplay() {
        return reducedDisplay;
    }

    public void changeMapLibrary(String mapLibrary, final AsyncCallback<Void> callback) {
        final EOLatLng center = getCenter();
        final int zoomLevel = getZoomLevel();
        final String mapId = getMapId();
        loadMapLibrary(mapLibrary, new Callback<Void, Exception>() {

            @Override
            public void onFailure(Exception reason) {
                callback.onFailure(new Exception("Could not load map library"));
            }

            @Override
            public void onSuccess(Void result) {
                createMap(new MapLoadedHandler() {

                    @Override
                    public void onLoad(UniMap uniMap) {
                        setCenter(center);
                        setZoomLevel(zoomLevel);
                        setMapId(mapId);
                        HashMap<AOI, FeatureRendering> aois = new HashMap<AOI, FeatureRendering>(featuresRendering);
                        ArrayList<ProductDisplay> products = new ArrayList<ProductDisplay>(productsRendering.keySet());
                        clearAll();
                        for(AOI aoi : aois.keySet()) {
                            FeatureRendering aoiRendering = aois.get(aoi);
                            if(aoiRendering.handler == null) {
                                addFeature(aoi);
                            } else {
                                addEditableFeature(aoi, aoiRendering.handler);
                            }
                        }
                        for(ProductDisplay product : products) {
                            addProduct(product);
                        }
                        updateDisplay();
                        callback.onSuccess(null);
                    }

                    @Override
                    public void onFailure() {
                        callback.onFailure(new Exception("Could not create new map"));
                    }
                });
            }
        });
    }

    public void outlineProduct(ProductDisplay product) {
        ProductDisplay oldOutlinedProduct = currentlyOutlinedProduct;
        currentlyOutlinedProduct = product;
        // update the map
        if(oldOutlinedProduct != null) {
            // change currently the highlighted product
            updateProductDisplay(oldOutlinedProduct);
        }
        for(AOI aoi : featuresRendering.keySet()) {
            if(aoi.isVisible()) {
                UniPolyline polyline = featuresRendering.get(aoi).getPolyline();
                if(polyline != null) {
                    polyline.setOnTop();
                }
            }
        }
        // dim all other products
        reducedDisplay = product != null;
        updateDisplay();
        if(product != null) {
            updateProductDisplay(product);
            // and then the selected product above
            productsRendering.get(product).polygon.setOnTop();
        }
    }

    public void enableFeatureInfo(boolean toggled, ToggledIconAnchor infoButton) {
        cancelFeatureInfoHandler();
        if(toggled) {
            featureInfoHandler = map.setClickHandler(new ClickHandler() {

                FeatureInfoPopup featureInfoPopup = FeatureInfoPopup.getInstance();

                @Override
                public void onMapClick(double lat, double lng) {
                    featureInfoPopup.showAt(infoButton, Util.TYPE.below);
                    featureInfoPopup.setLoading("Loading features");
                    final HashMap<UserLayerDTO, List<Feature>> featuresFound = new HashMap<>();
                    // scan all available layers and query the feature info at current position
                    final List<UserLayerDTO> wmsLayers = ListUtil.filterValues(new ArrayList<UserLayerDTO>(layersRendering.keySet()), new ListUtil.CheckValue<UserLayerDTO>() {
                        @Override
                        public boolean isValue(UserLayerDTO value) {
                            return value instanceof UserLayerDTO && ((UserLayerDTO) value).isVisible();
                        }
                    });
                    int size = 10;
                    double[] clientPos = map.convertEOLatLngToScreenPosition(lat, lng);
                    int clientX = (int) clientPos[0];
                    int clientY = (int) clientPos[1];
                    EOLatLng swLatLng = map.convertScreenPositionToEOLatLng(clientX - size, clientY + size);
                    EOLatLng neLatLng = map.convertScreenPositionToEOLatLng(clientX + size, clientY - size);
                    for (UserLayerDTO gisLayerImplementation : wmsLayers) {
                        final UserLayerDTO wmsLayer = ((UserLayerDTO) gisLayerImplementation);
                        try {
                            LayerDTO layer = wmsLayer.getLayerDTO();
                            new WMSCapabilities(layer.getBaseUrl()).getFeatureInfo(layer.getLayerName(),
                                    layer.getVersion(), "", "EPSG:4326",
                                    new EOBounds(neLatLng, swLatLng), 2 * size, 2 * size,
                                    size, size, new AsyncCallback<List<Feature>>() {

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            addResult(null);
                                        }

                                        @Override
                                        public void onSuccess(List<Feature> result) {
                                            addResult(result);
                                        }

                                        private void addResult(List<Feature> result) {
                                            if (result != null && result.size() > 0) {
                                                featuresFound.put(wmsLayer, result);
                                            } else {
                                                featuresFound.put(wmsLayer, null);
                                            }
                                            if(featuresFound.size() == wmsLayers.size()) {
                                                displayFeatures();
                                            }
                                        }

                                        private void displayFeatures() {
                                            featureInfoPopup.hideLoading();
                                            // now show the panel with features
                                            List<Feature> features = new ArrayList<Feature>();
                                            for (UserLayerDTO layerDTO : featuresFound.keySet()) {
                                                if(featuresFound.get(layerDTO) != null) {
                                                    features.addAll(featuresFound.get(layerDTO));
                                                }
                                            }
                                            if(features.size() == 0) {
                                                featureInfoPopup.displayMessage("No features found for the current layers");
                                            } else {
                                                featureInfoPopup.showFeatures(features);
                                            }
                                        }
                                    });
                        } catch (Exception e) {

                        }
                    }
                }

            });
            mapContainer.addStyleName("crosshairCursor");
        }
    }

    private void cancelFeatureInfoHandler() {
        if(featureInfoHandler != null) {
            featureInfoHandler.removeHandler();
        }
    }


    public void addLayer(UserLayerDTO userLayerDTO) {
        if(layersRendering.containsKey(userLayerDTO)) {
            return;
        }
        LayerDTO layer = userLayerDTO.getLayerDTO();
        if(layer.getLayerType() == LAYER_TYPE.WMS) {
            String layerBaseUrl = layer.getBaseUrl();
            // check if time enabled
            if(layer.isTimeEnabled()) {
                if(userLayerDTO.getSelectedTime() == null) {
                    userLayerDTO.setSelectedTime(userLayerDTO.getLayerDTO().getDates().get(0));
                }
                layerBaseUrl += "TIME=" + timeWMSFMT.format(userLayerDTO.getSelectedTime(), TimeZone.createTimeZone(0)) + "&";
            }
            UniWMSLayer uniWMSLayer = addWMSLayer(layerBaseUrl, layer.getLayerName(), layer.getVersion(), layer.getCredits(), null, "EPSG:3857", layer.getBounds());
            uniWMSLayer.setVisible(userLayerDTO.isVisible());
            // update display parameters
            uniWMSLayer.setOpacity(userLayerDTO.getOpacity() / 100.0);
            uniWMSLayer.setZIndex(userLayerDTO.getzIndex());
            if(!layer.isTimeEnabled()) {
                layersRendering.put(userLayerDTO, uniWMSLayer);
            } else {
                WMSTimeRendering wmsTimeRendering = new WMSTimeRendering();
                wmsTimeRendering.time = userLayerDTO.getSelectedTime();
                wmsTimeRendering.uniWMSLayer = uniWMSLayer;
                layersRendering.put(userLayerDTO, wmsTimeRendering);
            }
        }
    }

    public void updateLayer(UserLayerDTO layer) {
        Object rendering = layersRendering.get(layer);
        if(rendering == null) {
            return;
        }
        if(layer instanceof UserLayerDTO) {
            UniWMSLayer uniWMSLayer = null;
            if(rendering instanceof UniWMSLayer) {
                uniWMSLayer = (UniWMSLayer) rendering;
            } else if(rendering instanceof WMSTimeRendering) {
                WMSTimeRendering wmsTimeRendering = (WMSTimeRendering) rendering;
                // check if nearest time has changed
                Date selectedTime = layer.getSelectedTime();
                if(selectedTime != null && selectedTime.compareTo(wmsTimeRendering.time) != 0) {
                    // create new time layer
                    removeLayer(layer);
                    addLayer(layer);
                    // update the rendering object
                    wmsTimeRendering = (WMSTimeRendering) layersRendering.get(layer);
                }
                // add this at the end when everything has been updated
                uniWMSLayer = (UniWMSLayer) wmsTimeRendering.uniWMSLayer;
            }
            if(uniWMSLayer != null) {
                uniWMSLayer.setOpacity(layer.getOpacity() / 100.0);
                uniWMSLayer.setZIndex(layer.getzIndex());
            }
        } else {
            // TODO - do something?
        }
    }

    public void updateLayers() {
        for(UserLayerDTO layer : layersRendering.keySet()) {
            updateLayer(layer);
        }
    }

    public void removeLayer(UserLayerDTO layer) {
        clearLayer(layer);
        layersRendering.remove(layer);
    }

    public void removeAllLayers() {
        for(UserLayerDTO layer : layersRendering.keySet()) {
            clearLayer(layer);
        }
        layersRendering.clear();
    }

    private void clearLayer(UserLayerDTO layer) {
        Object rendering = layersRendering.get(layer);
        if(rendering == null) {
            return;
        }
        if(rendering instanceof UniWMSLayer) {
            removeOverlay(rendering);
        } else if(rendering instanceof WMSTimeRendering) {
            removeOverlay(((WMSTimeRendering) rendering).uniWMSLayer);
        }
    }

    public void clearLayers() {
        removeAllLayers();
    }

    public void clearAll() {
        clearFeatures();
        cleanUp();
        clearProducts();
        clearLayers();
        cancelFeatureInfoHandler();
    }

    public static double getSideLength(EOLatLng[] selectionGeometry) throws Exception {
        List<EOLatLng> corners = new ArrayList<EOLatLng>();
        for(int index = 0; index < selectionGeometry.length; index++) {
            EOLatLng tail = selectionGeometry[index];
            EOLatLng tip1 = selectionGeometry[index == 0 ? selectionGeometry.length - 1 : index - 1];
            EOLatLng tip2 = selectionGeometry[index < selectionGeometry.length - 1 ? index + 1 : 0];
            double angle = GeometryUtils.bearing(tail, tip2);
            angle -= GeometryUtils.bearing(tail, tip1);
            angle = Math.toDegrees(angle);
            angle = Math.abs(angle)%180;
            if(angle > 60 && angle < 120) {
                corners.add(tail);
            }
        }
        if(corners.size() != 4) {
            throw new Exception("Not the right number of corners, found " + corners.size() + " corners");
        }

        // now look for one of the south to north sides
        EOLatLng first = corners.get(0);
        EOLatLng second = corners.get(1);
        EOLatLng third = corners.get(2);
        double firstSecondAngle = Math.abs(Math.toDegrees(GeometryUtils.bearing(first, second)) % 180);
        if(firstSecondAngle > 90) {
            firstSecondAngle = 180 - firstSecondAngle;
        }
        double secondThirdAngle = Math.abs(Math.toDegrees(GeometryUtils.bearing(second, third)) % 180);
        if(secondThirdAngle > 90) {
            secondThirdAngle = 180 - secondThirdAngle;
        }
        // take the one closest to 90 degrees
        if(firstSecondAngle < secondThirdAngle) {
            return GeometryUtils.distance(first, second);
        } else {
            return GeometryUtils.distance(second, third);
        }
    }

}
