package com.geocento.webapps.earthimages.emis.application.client.views.viewpanels;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOIRectangle;
import com.geocento.webapps.earthimages.emis.application.client.event.LocationSelected;
import com.geocento.webapps.earthimages.emis.application.client.widgets.MapPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.map.LocationService;
import com.metaaps.webapps.libraries.client.widget.BasePopup;
import com.metaaps.webapps.libraries.client.widget.util.LastKeyPressed;
import com.metaaps.webapps.libraries.client.widget.util.Util;
import com.metaaps.webapps.libraries.client.widget.util.WidgetUtil;

import java.util.List;

/**
 * Created by thomas on 09/11/2015.
 */
public class LocationPopup extends Composite {

    interface LocationBoxUiBinder extends UiBinder<Widget, LocationPopup> {
    }

    private static LocationBoxUiBinder ourUiBinder = GWT.create(LocationBoxUiBinder.class);

    static private LocationService locationService = MapPanel.LocationService();

    static public interface Style extends CssResource {

        String locationLabel();

        String locationLabelSelected();

        String suggestions();
    }

    @UiField
    Style style;
    @UiField
    TextBox searchBox;
    @UiField
    FlowPanel suggestions;
    @UiField
    BasePopup popup;

    private MapPanel mapPanel;

    private EventBus eventBus;

    private String previousAddress = "";
    private int currentIndex = 0;
    private AOIRectangle aoi = new AOIRectangle();
    private List<LocationService.PlaceElement> locations;
    private int zoomLevel;
    private EOLatLng center;

    public LocationPopup() {

        ourUiBinder.createAndBindUi(this);

        searchBox.getElement().setAttribute("placeHolder", "Type place name...");
        searchBox.getElement().getStyle().setMargin(10, com.google.gwt.dom.client.Style.Unit.PX);

        popup.getElement().getStyle().setZIndex(3);
        popup.setAutoHideEnabled(true);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                hideLocation();
                mapPanel.setZoomLevel(zoomLevel);
                mapPanel.setCenter(center);
            }
        });

        WidgetUtil.setDelayedKeyUpHandler(searchBox, new LastKeyPressed() {

            @Override
            public void onKeyPressed(int nativeKeyCode) {
                // if key is a return key, select the first value
                boolean isKeyEnter = nativeKeyCode == KeyCodes.KEY_ENTER;
                if (isKeyEnter) {
                    // send a click to the selected label item in the popup
                    if (suggestions.getWidgetCount() > 0) {
                        suggestions.getWidget(currentIndex).fireEvent(new GwtEvent<ClickHandler>() {
                            @Override
                            public DomEvent.Type<ClickHandler> getAssociatedType() {
                                return ClickEvent.getType();
                            }

                            @Override
                            protected void dispatch(ClickHandler handler) {
                                handler.onClick(null);
                            }

                        });
                        searchBox.setFocus(false);
                    }
                } else if (nativeKeyCode == KeyCodes.KEY_DOWN) {
                    selectIndex(currentIndex + 1);
                } else if (nativeKeyCode == KeyCodes.KEY_UP) {
                    selectIndex(currentIndex - 1);
                } else {
                    String place = searchBox.getText();
//                    place.replace((char) KeyCodes.KEY_TAB, );
                    if (place.length() > 0 && place.compareTo(previousAddress) != 0) {
                        fetchLocations(searchBox.getText());
                    }
                }
            }

            private void selectIndex(int index) {
                currentIndex = Math.max(0, Math.min(index, suggestions.getWidgetCount() - 1));
                WidgetUtil.performAction(suggestions, new WidgetUtil.Action() {
                    @Override
                    public void action(Widget widget) {
                        widget.removeStyleName(style.locationLabelSelected());
                        if (suggestions.getWidgetIndex(widget) == currentIndex) {
                            widget.addStyleName(style.locationLabelSelected());
                            showLocation(currentIndex);
                        }
                    }
                });
            }

            private void fetchLocations(String address) {
                suggestions.clear();
                hideLocation();
                locationService.fetchLocations(address, new AsyncCallback<List<LocationService.PlaceElement>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        suggestions.add(new Label("No location found"));
                    }

                    @Override
                    public void onSuccess(List<LocationService.PlaceElement> result) {
                        locations = result;
                        if (result == null || result.size() == 0) {
                            suggestions.add(new Label("No location found"));
                        } else {
                            result = result.subList(0, Math.min(result.size(), 10));
                            for (LocationService.PlaceElement location : result) {
                                final String placeName = location.getPlaceName();
                                final Label locationLabel = new Label();
                                locationLabel.setText(placeName);
                                locationLabel.addStyleName(style.locationLabel());
                                WidgetUtil.registerHover(locationLabel, new MouseOverHandler() {

                                    @Override
                                    public void onMouseOver(MouseOverEvent event) {
                                        int index = suggestions.getWidgetIndex(locationLabel);
                                        showLocation(index);
                                    }
                                }, new MouseOutHandler() {
                                    @Override
                                    public void onMouseOut(MouseOutEvent event) {
                                    }
                                });
                                locationLabel.addClickHandler(new ClickHandler() {

                                    @Override
                                    public void onClick(ClickEvent event) {
                                        hideLocation();
                                        popup.hide();
                                        int index = suggestions.getWidgetIndex(locationLabel);
                                        eventBus.fireEvent(new LocationSelected(placeName, locations.get(index).getPlaceBoundaries()));
                                    }

                                });
                                suggestions.add(locationLabel);
                            }
                            selectIndex(0);
                        }
                    }

                });
            }

        });
    }

    private void hideLocation() {
        mapPanel.removeFeature(aoi);
    }

    private void showLocation(int index) {
        mapPanel.removeFeature(aoi);
        if (locations == null || locations.size() < index) {
            return;
        }
        EOBounds bounds = locations.get(index).getPlaceBoundaries();
        aoi.setStrokeColor("33CC33");
        mapPanel.setEOBounds(bounds);
        aoi.setBounds(bounds);
        mapPanel.addFeature(aoi);
    }

    public void setMapPanel(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void showAt(Widget widget, Util.TYPE type) {
        zoomLevel = mapPanel.getZoomLevel();
        center = mapPanel.getCenter();
        searchBox.setText("");
        suggestions.clear();
        suggestions.add(new Label("No place..."));
        popup.showAt(widget, type);
        searchBox.setFocus(true);
    }

}