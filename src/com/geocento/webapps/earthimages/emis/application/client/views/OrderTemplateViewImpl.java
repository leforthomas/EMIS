package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.event.MapLibraryChanged;
import com.geocento.webapps.earthimages.emis.application.client.event.OrderDisplaySettingsChanged;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.widgets.*;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.map.MapLoadedHandler;
import com.metaaps.webapps.libraries.client.map.UniMap;
import com.metaaps.webapps.libraries.client.widget.MessageLabel;
import com.metaaps.webapps.libraries.client.widget.SwitchWidget;
import com.metaaps.webapps.libraries.client.widget.util.ValueChangeHandler;
import com.metaaps.webapps.libraries.client.widget.util.WidgetUtil;

import java.util.Iterator;

/**
 * Created by thomas on 26/09/2014.
 */
public class OrderTemplateViewImpl extends Composite implements OrderTemplateView, IsWidget, HasWidgets, ResizeHandler {

    private StyleResources styleResources = StyleResources.INSTANCE;

    interface OrderTemplateViewUiBinder extends UiBinder<Widget, OrderTemplateViewImpl> {
    }

    private static OrderTemplateViewUiBinder ourUiBinder = GWT.create(OrderTemplateViewUiBinder.class);

    static public interface Style extends CssResource {
        String stepsDone();
        String currentStep();
        String cart();

        String orderingSteps();
    }

    static public enum ORDER_STEP {select, cart, payment, request, download};

    @UiField Style style;

    @UiField
    Footer footer;
    @UiField
    HTMLPanel content;
    @UiField
    Label selectStep;
    @UiField
    Label cartStep;
    @UiField
    Label paymentStep;
    @UiField
    Label downloadStep;
    @UiField
    HTMLPanel mapContainer;
    @UiField
    Label requestStep;
/*
    @UiField
    IconAnchor homeIcon;
*/
    @UiField
    HTMLPanel mainPanel;
    @UiField
    EILiteTemplateView templateView;
    @UiField
    HTMLPanel splitterBar;
    @UiField
    HTMLPanel contentPanel;
    @UiField
    MessageLabel loadingMessage;
    @UiField
    SwitchWidget switchPanels;
/*
    @UiField
    HTMLPanel contactInfoSales;
*/

    private ClientFactory clientFactory;

    private MapPanel mapPanel;
    protected MapTools mapTools;

    private OrderDisplaySettingsWidget displaySettings;

    public OrderTemplateViewImpl(final ClientFactory clientFactory) {

        this.clientFactory = clientFactory;

        initWidget(ourUiBinder.createAndBindUi(this));

        //contactInfoSales.getElement().setInnerText(Application.getApplicationSettings().getContactInfoSales());

        LoadingPanel.getInstance().getElement().getStyle().setZIndex(1000);

        //selectStep.getParent().getParent().addStyleName(style.orderingSteps());

        Window.addResizeHandler(this);

    }

    @Override
    public EILiteTemplateView getTemplateView() {
        return templateView;
    }

    public void setStep(ORDER_STEP step) {
        selectStep.getParent().setStyleName(style.cart(), false);
        cartStep.getParent().setStyleName(style.cart(), false);
        requestStep.getParent().setStyleName(style.cart(), false);
        paymentStep.getParent().setStyleName(style.cart(), false);
        downloadStep.getParent().setStyleName(style.cart(), false);
        switch (step) {
            case select:
                selectStep.setStyleName(style.currentStep());
                selectStep.getParent().addStyleName(style.cart());
                break;
            case cart:
                selectStep.setStyleName(style.stepsDone());
                cartStep.setStyleName(style.currentStep());
                cartStep.getParent().addStyleName(style.cart());
                break;
            case request:
                selectStep.setStyleName(style.stepsDone());
                cartStep.setStyleName(style.stepsDone());
                requestStep.setStyleName(style.currentStep());
                requestStep.getParent().addStyleName(style.cart());
                break;
            case payment:
                selectStep.setStyleName(style.stepsDone());
                cartStep.setStyleName(style.stepsDone());
                requestStep.setStyleName(style.stepsDone());
                paymentStep.setStyleName(style.currentStep());
                paymentStep.getParent().addStyleName(style.cart());
                break;
            case download:
                selectStep.setStyleName(style.stepsDone());
                cartStep.setStyleName(style.stepsDone());
                requestStep.setStyleName(style.stepsDone());
                paymentStep.setStyleName(style.stepsDone());
                downloadStep.setStyleName(style.currentStep());
                downloadStep.getParent().addStyleName(style.cart());
                break;
        }
    }

    public void setMapPanel(final MapPanel mapPanel, int minWidth) {
        setMapPanel(mapPanel, minWidth, true);
    }
    public void setMapPanel(final MapPanel mapPanel, int minWidth, boolean displayLayers) {
        this.mapPanel = mapPanel;
        mapContainer.add(mapPanel);
        mapContainer.add(splitterBar);
        mapTools = new MapTools();
        mapTools.setChangeLibraryEnabled(true);
        mapTools.setPresenter(new MapTools.Presenter() {

            @Override
            public void onBaseMapChanged(String mapId) {
            }

            @Override
            public void onLayerChanged(String layer) {
            }

            @Override
            public void onMapLibraryChanged(final String mapLibrary) {
                // save zoom level and position
                EOLatLng position = mapPanel.getCenter();
                int zoomLevel = mapPanel.getZoomLevel();
                mapPanel.changeMapLibrary(mapLibrary, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(Void result) {
                        mapPanel.addControl(mapTools);
                        mapPanel.setCenter(position);
                        mapPanel.setZoomLevel(zoomLevel);
                        mapPanel.updateDisplay();
                        clientFactory.getEventBus().fireEvent(new MapLibraryChanged(mapLibrary));
                    }
                });
            }

            @Override
            public void mapExtentChanged() {
            }

        });
        mapTools.displayLayersMenu(displayLayers);
        displaySettings = new OrderDisplaySettingsWidget();
        displaySettings.setImageResource(styleResources.settings());
        mapTools.add(displaySettings);
        displaySettings.setValueChangeHandler(new ValueChangeHandler<Long>() {

            @Override
            public void onValueChanged(Long value) {
                clientFactory.getEventBus().fireEvent(new OrderDisplaySettingsChanged(displaySettings.getOverlayOpacity(), displaySettings.getProductSelectionOpacity()));
            }
        });
        makeBannerResizable(mapPanel, minWidth);
        // set to min width by default
        setMapWidth(Math.max(50, Window.getClientWidth() - minWidth));
    }

    public Panel getMapContainer() {
        return mapContainer;
    }

    private void makeBannerResizable(MapPanel mapPanel, int minContentWidth) {
        splitterBar.clear();
        HTMLPanel panel = new HTMLPanel("") {

            public boolean dragged = false;
            public int previous;

            @Override
            public void onBrowserEvent(Event event) {
                if (DOM.eventGetType(event) == Event.ONMOUSEDOWN) {
                    Event.setCapture(this.getElement());
                    dragged = true;
                    previous = event.getScreenX();
                } else if (dragged) {
                    if (DOM.eventGetType(event) == Event.ONMOUSEMOVE) {
                        int width = mapContainer.getOffsetWidth() + (previous - event.getScreenX());
                        // make sure width is limited to the maximum of the windows width
                        width = Math.max(50, Math.min(width, Window.getClientWidth() - minContentWidth));
                        setMapWidth(width);
                        previous = event.getScreenX();
                    } else if (DOM.eventGetType(event) == Event.ONMOUSEUP) {
                        Event.releaseCapture(this.getElement());
                        dragged = false;
                        mapPanel.onResize();
                        WidgetUtil.performAction(mapContainer, widget -> {if(widget instanceof ResizeHandler) {
                            ((ResizeHandler) widget).onResize(null);
                        }
                        });
                    }
                }
                event.preventDefault();
                event.stopPropagation();
            }
        };
        panel.sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEMOVE | Event.ONMOUSEUP | Event.ONMOUSEOUT);
        panel.setHeight("100%");
        panel.getElement().getStyle().setCursor(com.google.gwt.dom.client.Style.Cursor.COL_RESIZE);
        splitterBar.add(panel);
    }

    private void setMapWidth(int width) {
        mapContainer.setWidth(width + "px");
        contentPanel.getElement().getStyle().setMarginRight(width + 20, com.google.gwt.dom.client.Style.Unit.PX);
    }

    public void loadMapLibrary(final MapPanel mapPanel, final String mapId, final AsyncCallback<Void> callBack) {
        // first load of map library
        mapPanel.loadMapLibrary(new Callback<Void, Exception>() {

            @Override
            public void onSuccess(Void result) {
                mapPanel.createMap(mapId == null || mapId.length() == 0 ? "hybrid" : mapId, new MapLoadedHandler() {

                    @Override
                    public void onLoad(UniMap uniMap) {
                        mapPanel.addControl(mapTools);
                        mapTools.setMapPanel(mapPanel);
                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {

                            @Override
                            public void execute() {
                                mapPanel.onResize();
                                callBack.onSuccess(null);
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        callBack.onFailure(new Exception("Could not create the map"));
                    }
                });
            }

            @Override
            public void onFailure(Exception reason) {
                callBack.onFailure(new Exception("Failed to load the map libraries"));
            }
        });

    }

    public void setDisplaySettings(double overlayTransparency, double productSelectionOpacity) {
        displaySettings.setOverlayOpacity(overlayTransparency);
        displaySettings.setProductSelectionOpacity(productSelectionOpacity);
    }

    public void displayWindowErrorMessage(String message) {
        Window.alert(message);
    }

    public void displayWindowLoading(String message) {
        LoadingPanel.getInstance().show(message);
    }

    public void hideWindowLoading() {
        LoadingPanel.getInstance().hide();
    }

    public void displayPageLoading(String message) {
        this.loadingMessage.displayLoading(message);
        switchPanels.showWidget(loadingMessage);
    }

    public void displayPageLoadingError(String message) {
        this.loadingMessage.displayError(message);
        switchPanels.showWidget(loadingMessage);
    }

    protected void showArrow(boolean topPosition, final Element element, final EOBounds bounds) {
        hideArrow();
        // show a line that connects the active alert widget to the footprint in the map
        String outlineColor = "#FFFFFF";
        double[] from = new double[] {
                topPosition ? (element.getAbsoluteLeft() + element.getOffsetWidth() / 2) : mapPanel.getElement().getAbsoluteLeft(),
                topPosition ? mapPanel.getElement().getAbsoluteTop() : (element.getAbsoluteTop() + element.getOffsetHeight() / 2)
        };
        EOBounds mapBounds = mapPanel.getMap().getEOBounds();
        if(!mapBounds.intersects(bounds)) {
            return;
        }
        EOBounds intersection = EOBounds.intersection(mapBounds, bounds);
        EOLatLng center = intersection.getCenter();
        double[] to = mapPanel.getMap().convertEOLatLngToScreenPosition(center.getLat(), center.getLng());
        ArrowPanel.getInstance().displayAt(topPosition, from[0], from[1], to[0], to[1], outlineColor);
    }

    protected void hideArrow() {
        ArrowPanel.getInstance().hide();
    }

    public void displayContent() {
        switchPanels.showWidget(mainPanel);
    }

    public void scrollTop() {
        mainPanel.getElement().setScrollTop(0);
    }

    @Override
    protected void onUnload() {
        super.onUnload();
    }

    @Override
    public void add(Widget w) {
        content.add(w);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return content.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return content.remove(w);
    }

    @Override
    public void onResize(ResizeEvent event) {
    }

}
