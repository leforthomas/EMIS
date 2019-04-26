package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.application.share.ProductMetadataDTO;
import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.google.gwt.dom.client.Element;
import com.metaaps.webapps.libraries.client.map.*;
import com.metaaps.webapps.libraries.client.map.utils.GeometryUtils;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.ValueChangeHandler;

import java.util.*;

public class MapOrderPanel extends MapPanel {

    static public interface Presenter {
        void onProductOrderClicked(ProductOrderDTO product, EOLatLng eoLatLng);
        void onProductOrderChanged(ProductOrderDTO product);
    }

    private static String productOrderStrokeColor = "#ffffff";
    private static String productOrderSelectionStrokeColor = "#00ff00";
    private static int productOrderStrokeThickness = 2;
    private static double productOrderStrokeOpacity = 1.0;
    private static String productOrderFillColor = "#00ff00";
    private static double productOrderFillOpacity = 0.3;

    private static double overlayOpacity = 0.9;

    private double featureOpacity = 0.0;

    private class ProductOrderRendering {
        UniPolyline outline;
        List<UniWMSLayer> overlays;
        Object selection;
        int zIndex;
    }

    private Set<ProductOrderDTO> visibleProductOrders = new HashSet<ProductOrderDTO>();

    private HashMap<ProductOrderDTO, ProductOrderRendering> productOrdersRendering = new HashMap<ProductOrderDTO, ProductOrderRendering>();

	private Presenter presenter;

    private ProductOrderDTO currentlyHighlightedProductOrder;

    public MapOrderPanel() {
	}
	
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	// TODO - fixed issue with Cesium with z-index and overlays which are not displayed
    public void addProductOrder(ProductOrderDTO productOrder) {
        if(map == null || productOrder == null) {
            return;
        }
        if(productOrdersRendering.containsKey(productOrder)) {
            return;
        }
        EOLatLng[] coordinates = productOrder.getCoordinates();
        if(coordinates == null) {
            return;
        }
        // add polygon with default values
        UniPolyline polyline = addPolyline(GeometryUtils.closePath(coordinates), "#ffffff", 1, 1.0, false, true);
        polyline.setOnTop();
        polyline.setTooltip("Product from " + productOrder.getDescription());
        ProductOrderRendering productRendering = new ProductOrderRendering();
        productRendering.outline = polyline;
        productRendering.zIndex = productOrdersRendering.size();
        ArrayList<ProductMetadataDTO> publishedProducts = productOrder.getPublishedProducts();
        // TODO - check the layer name is not null, etc...
        if(productOrder.getStatus() == PRODUCTORDER_STATUS.Completed && publishedProducts != null) {
            List<UniWMSLayer> overlays = new ArrayList<UniWMSLayer>();
            for(ProductMetadataDTO productMetadataDTO : publishedProducts) {
                UniWMSLayer overlay = addWMSLayer(productOrder.getProductWMSServiceURL(),
                        productMetadataDTO.getPublishUri(),
                        "1.1.1", null, null, "EPSG:3857", EOBounds.getBounds(coordinates));
                overlay.setZIndex(productRendering.zIndex * 10 + overlays.size());
                overlays.add(overlay);
            }
            productRendering.overlays = overlays;
        }
        productOrdersRendering.put(productOrder, productRendering);
        updateProductOrderSelectionDisplay(productOrder);
        updateProductOrderDisplay(productOrder);
    }

    public void updateProductOrderSelectionDisplay(final ProductOrderDTO productOrderDTO) {
        ProductOrderRendering rendering = productOrdersRendering.get(productOrderDTO);
        if (rendering == null) {
            return;
        }
        if (rendering.selection != null) {
            if(rendering.selection instanceof UniPolygon) {
                removeOverlay(rendering.selection);
            } else if(rendering.selection instanceof ConstrainedFrame) {
                ((ConstrainedFrame) rendering.selection).remove();
            }
        }

        EOLatLng[] coordinates = productOrderDTO.getCoordinates();

        ClickHandler polygonClickHandler = new ClickHandler() {
            @Override
            public void onMapClick(double lat, double lng) {
                if (presenter != null) {
                    presenter.onProductOrderClicked(productOrderDTO, new EOLatLng(lat, lng));
                }
            }
        };
    }

    public void setProductSelectionOpacity(double opacity) {
        productOrderFillOpacity = opacity;
    }

    public void setOverlaysOpacity(double opacity) {
        overlayOpacity = opacity;
    }

    @Override
    public void addFeature(AOI feature) {
        feature.setFillOpacity(featureOpacity);
        super.addFeature(feature);
    }

    @Override
    public void updateDisplay() {
        super.updateDisplay();
        updateProductOrdersDisplay();
        // make sure observations lines are on top
        for(AOI observation : featuresRendering.keySet()) {
            if(observation.isVisible()) {
                UniPolyline polyline = featuresRendering.get(observation).getPolyline();
                if(polyline != null) {
                    polyline.setOnTop();
                }
            }
        }
        // put selections on top
        for(ProductOrderDTO productOrderDTO : getProductsOrdersOrdered()) {
            if(isProductOrderVisible(productOrderDTO)) {
                Object selection = productOrdersRendering.get(productOrderDTO).selection;
                if(selection != null) {
                    if(selection instanceof UniPolygon) {
                        ((UniPolygon) selection).setOnTop();
                    } else if(selection instanceof ConstrainedFrame) {
                        ((ConstrainedFrame) selection).polygon.setOnTop();
                    }
                }
            }
        }
    }

    private List<ProductOrderDTO> getProductsOrdersOrdered() {
        ArrayList<ProductOrderDTO> productsOrdered = new ArrayList<ProductOrderDTO>(productOrdersRendering.keySet());
        Collections.sort(productsOrdered, new Comparator<ProductOrderDTO>() {
            @Override
            public int compare(ProductOrderDTO o1, ProductOrderDTO o2) {
                return productOrdersRendering.get(o1).zIndex < productOrdersRendering.get(o2).zIndex ? -1 : 1;
            }
        });
        return productsOrdered;
    }

    public boolean isProductOrderVisible(ProductOrderDTO productOrderDTO) {
        return productOrderDTO.isVisible();
    }

    private void updateProductOrdersDisplay() {
        for(ProductOrderDTO productOrderDTO : getProductsOrdersOrdered()) {
            updateProductOrderDisplay(productOrderDTO);
        }
    }

    private void updateProductOrderDisplay(ProductOrderDTO productOrderDTO) {
        ProductOrderRendering rendering = productOrdersRendering.get(productOrderDTO);
        if (rendering != null) {
            boolean isVisible = isProductOrderVisible(productOrderDTO);
            rendering.outline.setVisible(isVisible);
            List<UniWMSLayer> overlays = rendering.overlays;
            if(overlays != null) {
                for(UniWMSLayer overlay : overlays) {
                    overlay.setVisible(isVisible);
                }
            }
            if(rendering.selection != null) {
                if(rendering.selection instanceof UniPolygon) {
                    ((UniPolygon) rendering.selection).setVisible(isVisible);
                } else if(rendering.selection instanceof ConstrainedFrame) {
                    ((ConstrainedFrame) rendering.selection).setVisible(isVisible);
                }
            }
            if(isVisible) {
                rendering.outline.setStrokeStyle(productOrderStrokeColor, productOrderStrokeOpacity, productOrderStrokeThickness);
                if(overlays != null) {
                    for(UniWMSLayer overlay : overlays) {
                        overlay.setOpacity(overlayOpacity);
                    }
                }
                if(rendering.selection != null) {
                    if(rendering.selection instanceof UniPolygon) {
                        ((UniPolygon) rendering.selection).setFillOpacity(productOrderFillOpacity);
                    } else if(rendering.selection instanceof ConstrainedFrame) {
                        ((ConstrainedFrame) rendering.selection).setFillOpacity(productOrderFillOpacity);
                    }
                }
            }
        }
    }

    public void highlightProductOrder(ProductOrderDTO productOrderDTO) {
        // only one highlighted product at a time
        ProductOrderDTO oldHighlightedProduct = currentlyHighlightedProductOrder;
        currentlyHighlightedProductOrder = productOrderDTO;
        // update the map
        if(oldHighlightedProduct != null) {
            // change currently the highlighted product
            ProductOrderRendering rendering = productOrdersRendering.get(oldHighlightedProduct);
            if(rendering != null) {
                rendering.outline.setHighlighted(false);
                if(rendering.selection != null) {
                    if(rendering.selection instanceof UniPolygon) {
                        ((UniPolygon) rendering.selection).setEditable(false);
                        ((UniPolygon) rendering.selection).setHighlighted(false);
                    } else if(rendering.selection instanceof ConstrainedFrame) {
                        ((ConstrainedFrame) rendering.selection).setHighlighted(false);
                    }
                }
            }
        }
        if(productOrderDTO != null) {
            ProductOrderRendering rendering = productOrdersRendering.get(productOrderDTO);
            // and then the selected product above
            rendering.outline.setOnTop();
            if(rendering.selection != null) {
                if(rendering.selection instanceof UniPolygon) {
                    ((UniPolygon) rendering.selection).setEditable(true);
                    ((UniPolygon) rendering.selection).setHighlighted(true);
                    ((UniPolygon) rendering.selection).setOnTop();
                } else if(rendering.selection instanceof ConstrainedFrame) {
                    ((ConstrainedFrame) rendering.selection).setHighlighted(true);
                }
            }
        }
    }

/*
    public void setZIndex(ProductOrderDTO productOrder, int index) {
        ProductOrderRendering rendering = productOrdersRendering.get(productOrder);
        if(rendering != null) {
            for(UniWMSLayer overlay : rendering.overlays) {
                overlay.setZIndex(index);
            }
        }
    }

*/
    public void removeProductOrder(ProductOrderDTO productOrder) {
        ProductOrderRendering rendering = productOrdersRendering.get(productOrder);
        if(rendering != null) {
            removeOverlay(rendering.outline);
            if(rendering.overlays != null) {
                for(UniWMSLayer overlay : rendering.overlays) {
                    removeOverlay(overlay);
                }
            }
            if(rendering.outline != null) {
                removeOverlay(rendering.outline);
            }
            if(rendering.selection != null) {
                if(rendering.selection instanceof UniPolygon) {
                    removeOverlay(rendering.selection);
                } else if(rendering.selection instanceof ConstrainedFrame) {
                    ((ConstrainedFrame) rendering.selection).remove();
                }
            }
            productOrdersRendering.remove(productOrder);
        }
        updateDisplay();
    }

    public void activateClipping(ProductOrderDTO productOrder, boolean selected) {
        ProductOrderRendering rendering = productOrdersRendering.get(productOrder);
        if(rendering != null) {
            if(rendering.overlays != null) {
                for(UniWMSLayer uniWMSLayer : rendering.overlays) {
                    activateClipping(uniWMSLayer, selected);
                }
            }
        }
    }

    private native void activateClipping (UniWMSLayer uniWMSLayer, boolean selected) /*-{
        uniWMSLayer.activateClipping(selected);
    }-*/;

    @Override
    public void cleanUp() {
        super.cleanUp();
        clearProductOrders();
    }

    public void clearProductOrders() {
        for(ProductOrderDTO productOrderDTO : new ArrayList<ProductOrderDTO>(productOrdersRendering.keySet())) {
            removeProductOrder(productOrderDTO);
        }
    }

    public void activateWCSSelection(ProductOrderDTO productOrder, boolean selected) {
        ProductOrderRendering rendering = productOrdersRendering.get(productOrder);
        if(!selected && rendering.selection != null) {
            removeOverlay(rendering.selection);
        }
    }

    public EOBounds getWCSBounds() {
        return null;
    }

    // TODO - move to map implementation?
    class ConstrainedFrame {

        EOLatLng frameTopLeft = null, frameTopRight = null, frameBottomLeft = null, frameBottomRight = null;
        ArrayList<EOLatLng> leftSideInterior;
        ArrayList<EOLatLng> rightSideInterior;
        EOLatLng topLeft, topRight, bottomLeft, bottomRight;

        private UniPolygon polygon;
        private UniMarker topMarker;
        private UniMarker bottomMarker;
        private ValueChangeHandler<EOLatLng[]> frameChangedHandler;

        public ConstrainedFrame(EOLatLng[] frameCoordinates, boolean descending, EOLatLng[] selectionCoordinates, String color, int thickness, double opacity, String fillColor, double fillOpacity) {
            EOLatLng[] corners = getCorners(frameCoordinates); //GeometryUtils.getCorners(frameCoordinates);
            frameTopLeft = corners[0];
            frameTopRight = corners[1];
            frameBottomRight = corners[2];
            frameBottomLeft = corners[3];
            // now construct the two sides
            // sides are ordered from bottom to top
            final List<EOLatLng> leftSide = new ArrayList<EOLatLng>();
            List<EOLatLng> rightSide = new ArrayList<EOLatLng>();
            // find out if we are doing topLeft to bottomLeft or topLeft to topRight
            boolean clockWise = true;
            // check who comes first after top left
            boolean bottomLeftPassed = false;
            for(int index = 0; index < 2 * frameCoordinates.length; index++) {
                EOLatLng position = frameCoordinates[index % frameCoordinates.length];
                if(position == frameBottomLeft) {
                    bottomLeftPassed = true;
                }
                if(bottomLeftPassed && position == frameTopLeft) {
                    clockWise = true;
                    break;
                }
                if(bottomLeftPassed && position == frameBottomRight) {
                    clockWise = false;
                    break;
                }
            }
            boolean start = false;
            leftSide.add(frameBottomLeft);
            for(int index = 0; index < 2 * frameCoordinates.length; index++) {
                EOLatLng position = frameCoordinates[index % frameCoordinates.length];
                // if clockwise look for bottom left first
                if(clockWise) {
                    if(position == frameBottomLeft) {
                        start = true;
                    } else if(position == frameTopLeft) {
                        start = false;
                    } else if(start) {
                        leftSide.add(position);
                    }
                } else {
                    if(position == frameTopLeft) {
                        start = true;
                    } else if(position == frameBottomLeft) {
                        start = false;
                    } else if(start) {
                        leftSide.add(1, position);
                    }
                }
            }
            leftSide.add(frameTopLeft);
            start = false;
            rightSide.add(frameBottomRight);
            for(int index = 0; index < 2 * frameCoordinates.length; index++) {
                EOLatLng position = frameCoordinates[index % frameCoordinates.length];
                // if clockwise look for top right first
                if(clockWise) {
                    if(position == frameTopRight) {
                        start = true;
                    } else if(position == frameBottomRight) {
                        start = false;
                    } else if(start) {
                        rightSide.add(1, position);
                    }
                } else {
                    if(position == frameBottomRight) {
                        start = true;
                    } else if(position == frameTopRight) {
                        start = false;
                    } else if(start) {
                        rightSide.add(position);
                    }
                }
            }
            rightSide.add(frameTopRight);

            // remove the corners to coordinates
            leftSideInterior = new ArrayList<EOLatLng>();
            for(int index = 1; index < leftSide.size() - 1; index++) {
                leftSideInterior.add(leftSide.get(index));
            }
            rightSideInterior = new ArrayList<EOLatLng>();
            for(int index = 1; index < rightSide.size() - 1; index++) {
                rightSideInterior.add(rightSide.get(index));
            }

            // create the selection coordinates and update the corners
            List<EOLatLng> coordinates = new ArrayList<EOLatLng>();
            if(selectionCoordinates == null || selectionCoordinates.length < 4) {
                coordinates.addAll(leftSide);
                int nextPos = coordinates.size();
                for(EOLatLng position : rightSide) {
                    // inserts in reverse order the right side positions
                    coordinates.add(nextPos, position);
                }
                topLeft = frameTopLeft;
                topRight = frameTopRight;
                bottomLeft = frameBottomLeft;
                bottomRight = frameBottomRight;
            } else {
                coordinates.addAll(ListUtil.toList(selectionCoordinates));
                EOLatLng[] selectionCorners = getCorners(selectionCoordinates);
                topLeft = selectionCorners[0];
                topRight = selectionCorners[1];
                bottomRight = selectionCorners[2];
                bottomLeft = selectionCorners[3];
            }
            polygon = addPolygon(coordinates.toArray(new EOLatLng[coordinates.size()]), color, thickness, opacity, fillColor, fillOpacity, false, true);
            polygon.setTooltip("Your frame selection, drag the markers to change the selections");

            MarkerCompletedHandler markerCompletedHandler = new MarkerCompletedHandler() {
                @Override
                public void onMarkerCompleted(EOLatLng position) {
                    if(frameChangedHandler != null) {
                        List<EOLatLng> positions = polygon.getPositions();
                        frameChangedHandler.onValueChanged(positions.toArray(new EOLatLng[positions.size()]));
                    }
                }
            };
            topMarker = addEditableMarker(markerCompletedHandler, GeometryUtils.halfWay(topLeft, topRight), "./img/dragIcon.png", 5, 5);
            bottomMarker = addEditableMarker(markerCompletedHandler, GeometryUtils.halfWay(bottomLeft, bottomRight), "./img/dragIcon.png", 5, 5);

            MarkerEventHandler markerEventHandler = new MarkerEventHandler() {

                @Override
                public void onEvent(UniMarker marker) {
                    // take the new position
                    EOLatLng position = marker.getEOLatLng();
                    // project it against the left side
                    double ratio = GeometryUtils.getProjectedRatio(frameBottomLeft, frameTopLeft, position);
                    if(marker == topMarker) {
                        topLeft = GeometryUtils.interpolate(frameBottomLeft, frameTopLeft, ratio);
                        topRight = GeometryUtils.interpolate(frameBottomRight, frameTopRight, ratio);
                        topMarker.setPosition(GeometryUtils.halfWay(topLeft, topRight));
                    } else {
                        bottomLeft = GeometryUtils.interpolate(frameBottomLeft, frameTopLeft, ratio);
                        bottomRight = GeometryUtils.interpolate(frameBottomRight, frameTopRight, ratio);
                        bottomMarker.setPosition(GeometryUtils.halfWay(bottomLeft, bottomRight));
                    }
                    // fetch all points from coordinates which are between the current corners
                    EOLatLng[] points = fetchPolygonPoints();
                    polygon.updatePositions(points);
                }

            };
            topMarker.addEventHandler("drag", markerEventHandler);
            bottomMarker.addEventHandler("drag", markerEventHandler);
            setEditMode(false);
        }

        private EOLatLng[] getCorners(EOLatLng[] frameCoordinates) {
            try {
                return GeometryUtils.getCornersFromAngles(frameCoordinates);
            } catch(Exception e) {
                return GeometryUtils.getCorners(frameCoordinates);
            }
        }

        public void remove() {
            if(polygon != null) {
                removeOverlay(polygon);
                removeOverlay(topMarker);
                removeOverlay(bottomMarker);
            }
        }

        private EOLatLng[] fetchPolygonPoints() {
            double minDistance = bottomLeft.distanceFrom(frameBottomLeft);
            double maxDistance = topLeft.distanceFrom(frameBottomLeft);
            List<EOLatLng> coordinates = new ArrayList<EOLatLng>();
            coordinates.add(bottomLeft);
            for(EOLatLng position : leftSideInterior) {
                // check if position is within the ratios
                double distance = position.distanceFrom(frameBottomLeft);
                if(distance > minDistance && distance < maxDistance) {
                    coordinates.add(position);
                }
            }
            coordinates.add(topLeft);
            coordinates.add(topRight);
            minDistance = bottomRight.distanceFrom(frameBottomRight);
            maxDistance = topRight.distanceFrom(frameBottomRight);
            int nextPos = coordinates.size();
            for(EOLatLng position : rightSideInterior) {
                double distance = position.distanceFrom(frameBottomRight);
                if(distance > minDistance && distance < maxDistance) {
                    // inserts in reverse order the right side positions
                    coordinates.add(nextPos, position);
                }
            }
            coordinates.add(bottomRight);
            return coordinates.toArray(new EOLatLng[coordinates.size()]);
        }

        public void addClickHandler(ClickHandler clickHandler) {
            polygon.addClickHandler(clickHandler);
        }

        public void setFrameChangedHandler(ValueChangeHandler<EOLatLng[]> frameChangedHandler) {
            this.frameChangedHandler = frameChangedHandler;
        }

        public void setHighlighted(boolean highlighted) {
            if(polygon != null) {
                polygon.setHighlighted(highlighted);
                setEditMode(highlighted);
                if(highlighted) {
                    polygon.setOnTop();
                }
            }
        }

        private void setEditMode(boolean edit) {
            // show the markers
            topMarker.setVisible(edit);
            bottomMarker.setVisible(edit);
        }

        public void setVisible(boolean show) {
            if(polygon != null) {
                if(!show) {
                    setEditMode(false);
                    setHighlighted(false);
                }
                polygon.setVisible(show);
            }
        }

        public void setFillOpacity(double productOrderFillOpacity) {
            polygon.setFillOpacity(productOrderFillOpacity);
        }
    }

    public final native void setCaptureEvent(Element element, ClickHandler clickHandler) /*-{
        if(element && element.addEventListener) {
            var callback = function(e) {
                if(e.shiftKey) {
                    e.preventDefault();
                    e.stopPropagation();
                    clickHandler.@com.metaaps.webapps.libraries.client.map.ClickHandler::onMapClick(DD)(e.screenX, e.screenY);
                }
            }
            element.addEventListener("click", callback, true);
            //element.addEventListener("DOMMouseScroll", callback, true);
        }
    }-*/;

}
