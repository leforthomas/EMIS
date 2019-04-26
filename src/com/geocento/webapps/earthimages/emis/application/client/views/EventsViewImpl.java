package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.application.share.EventSummaryDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.widgets.*;
import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.MessageLabel;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 26/09/2014.
 */
public class EventsViewImpl extends Composite implements EventsView, ResizeHandler {

    interface OrdersViewImplUiBinder extends UiBinder<Widget, EventsViewImpl> {
    }

    private static OrdersViewImplUiBinder ourUiBinder = GWT.create(OrdersViewImplUiBinder.class);

    static public interface Style extends CssResource {

        String producOrder();

        String error();
    }

    @UiField
    Style style;

    @UiField(provided = true)
    OrderTemplateViewImpl templateView;
    @UiField
    Label comment;
    @UiField
    HTMLPanel content;
    @UiField(provided = true)
    OrdersList ordersList;
    @UiField
    Anchor viewMap;
    @UiField
    TextBox filterName;
    @UiField
    ListBox listBoxStatus;
    @UiField
    Anchor setFilter;
    @UiField
    MessageLabel loadingMessage;
    @UiField
    DateRangePicker dateRangePicker;

    private final ClientFactory clientFactory;

    private MapOrderPanel mapPanel;

    private StyleResources styleResources = StyleResources.INSTANCE;

    private Presenter presenter;

    public EventsViewImpl(final ClientFactory clientFactory) {

        this.clientFactory = clientFactory;

        templateView = new OrderTemplateViewImpl(clientFactory);

        ordersList = new OrdersList(new OrdersList.Presenter() {
            @Override
            public void handleSelection(EventSummaryDTO eventSummaryDTO) {
                //EventsViewImpl.this.selectOrder(eventSummaryDTO);
                //mapPanel.setEOBounds(eventSummaryDTO.getBounds());
            }

            @Override
            public void onMouseOver(Element element, EventSummaryDTO value) {
                ordersList.highlightOrder(value);
            }

            @Override
            public void onMouseOut(Element parent, EventSummaryDTO value) {

            }

            @Override
            public void selectOrder(EventSummaryDTO eventSummaryDTO) {
                EventsViewImpl.this.selectOrder(eventSummaryDTO);
                mapPanel.setEOBounds(eventSummaryDTO.getBounds());
            }

            @Override
            public void openOrder(EventSummaryDTO eventSummaryDTO) {
            }

            @Override
            public void zoomToOrder(EventSummaryDTO eventSummaryDTO) {
                EventsViewImpl.this.zoomToOrder(eventSummaryDTO);
            }

            @Override
            public void deleteOrder(EventSummaryDTO eventSummaryDTO)
            {
                presenter.deleteEvent(eventSummaryDTO.getId());
            }
        });

        initWidget(ourUiBinder.createAndBindUi(this));

        templateView.displayContent();
        templateView.setStep(OrderTemplateViewImpl.ORDER_STEP.download);

        displayOrdersLoading("Loading...");

        mapPanel = new MapOrderPanel();
        mapPanel.setPresenter(new MapOrderPanel.Presenter() {
            @Override
            public void onProductOrderClicked(ProductOrderDTO productOrder, EOLatLng eoLatLng) {

            }

            @Override
            public void onProductOrderChanged(ProductOrderDTO product) {

            }
        });
        templateView.setMapPanel(mapPanel, 770);

//        viewMap.addClickHandler(clickEvent -> Application.clientFactory.getPlaceController().goTo(new MapViewerPlace()));

        listBoxStatus.addItem("ALL", "ALL");

        // define default values for the datepicker
        Date now = new Date();
        dateRangePicker.setStartDate(DateUtil.addMonths(now, -12));
        dateRangePicker.setStopDate(DateUtil.addDays(now, 0));
        dateRangePicker.setMaxDate(now);
        dateRangePicker.setPresenter((startDate, stopDate) -> {
            presenter.timeFrameChanged(startDate, stopDate);
        });

        for (ORDER_STATUS status : ORDER_STATUS.values()) {
            listBoxStatus.addItem(status.toString(), status.toString());
        }

        templateView.displayContent();

        clearAll();

        LoadingPanel.getInstance().getElement().getStyle().setZIndex(1000);

        Window.addResizeHandler(this);

    }

    private void zoomToOrder(EventSummaryDTO eventSummaryDTO) {
        setMapBounds(eventSummaryDTO.getBounds());
    }

    @Override
    public void setMapBounds(EOBounds bounds) {
        mapPanel.setEOBounds(bounds);
    }

    @Override
    public void setBaseMapId(String baseMapId) {

    }

    @Override
    public void setOrders(List<EventSummaryDTO> eventSummaryDTO) {
        ordersList.setOrders(eventSummaryDTO);
    }

    @Override
    public void displayOrdersLoading(String message) {
        this.loadingMessage.displayLoading(message);
    }

    @Override
    public void displayOrdersMessage(String message) {
        this.loadingMessage.displayMessage(message);
    }

    @Override
    public void displayComment(String message) {
        comment.setVisible(true);
        comment.setText(message);
    }

    @Override
    public void displayOrderContent() {
        templateView.displayContent();
    }

    @Override
    public void selectOrder(EventSummaryDTO eventSummaryDTO) {
        ordersList.highlightOrder(eventSummaryDTO);
        mapPanel.clearFeatures();
        List<AOI> aois = eventSummaryDTO.getAois();
        for (AOI aoi : aois)
            mapPanel.addFeature(aoi);
        mapPanel.updateDisplay();
    }

    @Override
    public void setTimeFrame(Date startTime, Date stopTime) {
        dateRangePicker.setStartDate(startTime);
        dateRangePicker.setStopDate(stopTime);
        dateRangePicker.setLabel(dateRangePicker.formatDate(startTime) + " - " + dateRangePicker.formatDate(stopTime));
    }

    @Override
    public HasClickHandlers getSetFilter() {
        return setFilter;
    }

    @Override
    public OrderTemplateViewImpl getTemplateView() {
        return templateView;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void loadMapLibrary(final String mapId, final AsyncCallback<Void> callBack) {
        templateView.loadMapLibrary(mapPanel, mapId, callBack);
    }

    @Override
    public void setMapPanelDisplaySettings(double overlayTransparency, double productSelectionOpacity) {
        templateView.setDisplaySettings(overlayTransparency, productSelectionOpacity);
        mapPanel.setOverlaysOpacity(overlayTransparency);
        mapPanel.setProductOpacity(productSelectionOpacity);
        mapPanel.updateDisplay();
    }


    @Override
    public void clearAll() {
        ordersList.clearAll();
    }

    private void setError(TextBox textBox, boolean error) {
        textBox.setStyleName(style.error(), error);
    }

    @Override
    public void displayLoading(String message) {
        templateView.displayWindowLoading(message);
    }

    @Override
    public void hideLoading() {
        templateView.hideWindowLoading();
    }

    public void displayErrorMessage(String message) {
        loadingMessage.displayError(message);
        //switchPanels.showWidget(loadingMessage);
    }

    @Override
    public void onResize(ResizeEvent event) {

    }

    public HasText getNameFilter() {
        return filterName;
    }

    public String getStatusFilter() {
        return listBoxStatus.getSelectedValue();
    }

}
