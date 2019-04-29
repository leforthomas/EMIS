package com.geocento.webapps.earthimages.emis.application.client.activities;

import com.geocento.webapps.earthimages.emis.common.share.EILoginException;
import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PUBLICATION_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.event.*;
import com.geocento.webapps.earthimages.emis.application.client.place.ViewEventPlace;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.utils.HubspotChatHelper;
import com.geocento.webapps.earthimages.emis.application.client.utils.SettingsHelper;
import com.geocento.webapps.earthimages.emis.application.client.views.ViewEventView;
import com.geocento.webapps.earthimages.emis.application.client.widgets.MapPanel;
import com.geocento.webapps.earthimages.emis.application.client.widgets.Toast;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.geocento.webapps.earthimages.emis.application.share.websockets.ProductOrderNotification;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class ViewEventActivity extends EMISTemplateActivity implements ViewEventView.Presenter {

    private final ViewEventPlace place;

    private ViewEventView viewEventView;

    public EventDTO eventDTO;
    private Timer reloadTimer;

    public ViewEventActivity(ViewEventPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
        // show chat widget for cart
        HubspotChatHelper.displayChat(true);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        viewEventView = clientFactory.getViewEventView();
        viewEventView.setPresenter(this);
        panel.setWidget(viewEventView.asWidget());
        initialiseTemplate(viewEventView.getTemplateView().getTemplateView());
        viewEventView.clearAll();
        viewEventView.displayEventContent();
        Window.setTitle("EMIS - View Event and Satellite Imagery");
        bind();
        SettingsHelper.loadCookies();
        viewEventView.setMapPanelDisplaySettings(SettingsHelper.getOverlayTransparency(), SettingsHelper.getProductSelectionOpacity());
        viewEventView.displayMessageLoading("Loading map libraries...");
        viewEventView.loadMapLibrary(SettingsHelper.getBaseMapId(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                viewEventView.hideWindowLoading();
                viewEventView.displayWindowErrorMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                viewEventView.hideWindowLoading();
                handleHistory();
            }
        });
    }

    private void handleHistory() {
        // get the parameters
        HashMap<String, String> tokens = Utils.extractTokens(place.getToken());

        // set default settings first
        viewEventView.setMapBounds(
                new EOBounds(new EOLatLng(-1.0, -180.0), new EOLatLng(1.0, 180.0))
        );

        // get the event id
        if(tokens.containsKey(ViewEventPlace.TOKENS.eventid.toString())) {
            String orderId = tokens.get(ViewEventPlace.TOKENS.eventid.toString());
            loadEvent(orderId);
        } else {
            viewEventView.displayWindowErrorMessage("No order specified!");
        }
    }

    private void loadEvent(final String eventId) {
        loadEvent(eventId, null);
    }

    private void loadEvent(final String eventId, String password) {

        viewEventView.displayLoadingPage("Loading...");

        CustomerService.App.getInstance().loadEvent(eventId, password, new AsyncCallback<EventDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof EILoginException) {
                    viewEventView.displayEventContent();
                    viewEventView.requestPassword(new AsyncCallback<String>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            viewEventView.displayWindowErrorMessage("Event could not be loaded, no password provided");
                        }

                        @Override
                        public void onSuccess(String password) {
                            if (password != null) {
                                loadEvent(eventId, password);
                            } else {
                                viewEventView.displayWindowErrorMessage("Event could not be loaded, no password provided");
                            }
                        }
                    });
                    return;
                }
                viewEventView.hideWindowLoading();
                viewEventView.displayPageLoadingError("Failed to load event, reason is: " + caught.getMessage());
            }

            @Override
            public void onSuccess(EventDTO result) {
                viewEventView.displayEventContent();
                viewEventView.clearAll();
                ViewEventActivity.this.eventDTO = result;
                viewEventView.setEventDescription(eventDTO.getEventDescription());
                List<ProductOrderDTO> productOrders = result.getProductsOrdered();
                // set time grid
                // find the min and max
                ProductOrderDTO product = productOrders.get(0);
                Date minDate = product.getOriginalProductAcquisitionTime();
                Date maxDate = minDate;
                for (ProductOrderDTO productOrderDTO : productOrders) {
                    Date acquisitionTime = productOrderDTO.getOriginalProductAcquisitionTime();
                    if (minDate.after(acquisitionTime)) {
                        minDate = acquisitionTime;
                    }
                    if (maxDate.before(acquisitionTime)) {
                        maxDate = acquisitionTime;
                    }
                }
                // add a bit of margin on both sides
                double duration = Math.max(3600, maxDate.getTime() - minDate.getTime());
                minDate = new Date(minDate.getTime() - (long) (0.1 * duration));
                maxDate = new Date(maxDate.getTime() + (long) (0.1 * duration));
                viewEventView.setTimeGridTimeFrame(minDate, maxDate);
                // now add product orders
                for (ProductOrderDTO productOrderDTO : productOrders) {
                    // hide by default
                    productOrderDTO.setVisible(false);
                    viewEventView.addProductOrder(productOrderDTO);
                }
                updateOrderMessages();
                viewEventView.setComments(null);
                viewEventView.hideWindowLoading();
                displayAllSelections(true);
                displayAll(false);
                zoomProducts(result.getProductsOrdered());
            }
        });
    }

    private void updateOrderMessages() {
    }

    private void updateFundsPayments() {
        try {
            LoginInfo loginInfo = com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo();
            // check if we have products to pay for
            int productsToPay = 0;
            Price priceToPay = new Price(0, loginInfo.getPrepaidCurrency());
            for (ProductOrderDTO productOrderDTO : eventDTO.getProductsOrdered()) {
                if (productOrderDTO.getStatus() == PRODUCTORDER_STATUS.Quoted) {
                    priceToPay.add(productOrderDTO.getConvertedOfferedPrice());
                    productsToPay++;
                }
            }
            // display the current prepaid account value
            Price availableFunds = null;
            if (loginInfo.getPrepaidValue() == 0 || loginInfo.getPrepaidCurrency() == null) {
                viewEventView.setAvailableFunds(null);
            } else {
                availableFunds = new Price(loginInfo.getPrepaidValue(), loginInfo.getPrepaidCurrency());
                viewEventView.setAvailableFunds(availableFunds);
            }
            if (productsToPay > 0) {
                viewEventView.displayPayment(true);
                boolean sufficientFunds = availableFunds != null && availableFunds.getValue() > priceToPay.getValue();
                viewEventView.displayPaymentMessage("You have " + productsToPay + " products quoted, for a total of " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(priceToPay) +
                        ". " + (availableFunds == null || availableFunds.getValue() == 0 ? "You have no funds left." : "You have " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(availableFunds) + " on your prepaid account.") +
                        (sufficientFunds ? "" : " You need to add funds to your account to make this payment."));
                viewEventView.displayPayPrepaid(sufficientFunds);
            }
        } catch (Exception e) {

        }
    }

    private void zoomProducts(List<ProductOrderDTO> productOrders) {
        EOBounds eoBounds = new EOBounds();
        for(ProductOrderDTO product : productOrders) {
            eoBounds.extend(EOBounds.getBounds(product.getCoordinates()));
        }
        viewEventView.setMapBounds(eoBounds);
    }

    @Override
    public void archiveEvent() {
        if(Window.confirm("Are you sure you want to archive this event?")) {

            viewEventView.displayLoadingPage("Archiving your event...");
            CustomerService.App.getInstance().archiveEvent(eventDTO.getId(), new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    viewEventView.hideWindowLoading();
                    viewEventView.displayWindowErrorMessage("Could not archive order, please try again");
                }

                @Override
                public void onSuccess(Void result) {
                    viewEventView.hideWindowLoading();
                    viewEventView.setOrderStatus(ORDER_STATUS.ARCHIVED);
                }
            });
        }
    }

    @Override
    public void deleteProduct(String id) {
        if(Window.confirm("Are you sure you want to delete this product?")) {

            viewEventView.displayLoadingPage("Deleting product...");
            CustomerService.App.getInstance().deleteProduct(id, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    viewEventView.hideWindowLoading();
                    viewEventView.displayWindowErrorMessage("Could not delete product, please try again");
                }

                @Override
                public void onSuccess(Void result) {
                    viewEventView.hideWindowLoading();
                    viewEventView.deleteProductOrder(id);
                }
            });
        }
    }

    @Override
    public void changePackageName(String oldPackageName, String packageName) {
        // TODO - update all product order with the package name
    }

    @Override
    public void handleWCSRequest(EOBounds bounds) {
        ProductOrderDTO selectedProductOrder = null;
        for(ProductOrderDTO productOrder : eventDTO.getProductsOrdered()) {
            if(productOrder.isVisible()) {
                EOBounds productBounds = EOBounds.getBounds(productOrder.getCoordinates());
                if (bounds.intersects(productBounds)) {
                    selectedProductOrder = productOrder;
                    break;
                }
            }
        }
        if(selectedProductOrder == null) {
            Window.alert("Your selection doesn't interesect with any of the product orders!");
            return;
        }
        // calculate the intersection
        EOBounds productBounds = EOBounds.getBounds(selectedProductOrder.getCoordinates());
        EOBounds intersectionBounds = EOBounds.intersection(productBounds, bounds);
        double area = MapPanel.getPathArea(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.generateNonSphericalRectangleCoordinates(intersectionBounds, true));
        ProductOrderDTO finalSelectedProductOrder = selectedProductOrder;
        viewEventView.getWCSFormat("Download sub area",
                "You have selected " + Utils.formatSurface(Utils.FORMAT.SQKILOMETERS, 1, area) + " of image from product order #" + selectedProductOrder.getId() + ".",
                new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {

                    }

                    @Override
                    public void onSuccess(String format) {
                        Extent extent = new Extent();
                        extent.setSouth(intersectionBounds.getCoordinatesSW().getLat());
                        extent.setWest(intersectionBounds.getCoordinatesSW().getLng());
                        extent.setNorth(intersectionBounds.getCoordinatesNE().getLat());
                        extent.setEast(intersectionBounds.getCoordinatesNE().getLng());
                        String extentString = ((Extent.ExtentMapper) GWT.create(Extent.ExtentMapper.class)).write(extent);
                        extentString = URL.encodeQueryString(extentString);
                        Window.open("./api/download-product/download/" + finalSelectedProductOrder.getId() + "/truecolour/selection/" + format + "?extent=" + extentString, "_blank", null);
                    }
                });
    }

    @Override
    protected void bind() {

        activityEventBus.addHandler(MapLibraryChanged.TYPE, event -> {
        });

        activityEventBus.addHandler(OrderDisplaySettingsChanged.TYPE, event -> changeDisplaySettings(event.getOverlayOpacity(), event.getProductSelectionOpacity()));

        activityEventBus.addHandler(PublishProductOrder.TYPE, event -> {
            ProductOrderDTO productOrderDTO = event.getProductOrder();
            CustomerService.App.getInstance().republishProductOrder(productOrderDTO.getId(), new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Could not republish product order, reason is " + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    productOrderDTO.setPublicationStatus(PUBLICATION_STATUS.Requested);
                    productOrderDTO.setPublishedProducts(null);
                    viewEventView.updateProductOrderPublishStatuses(productOrderDTO);
                }
            });
        });

        activityEventBus.addHandler(TimeChangedEvent.TYPE, event -> {
            setCurrentTime(event.getDate());
        });

        handlers.add(viewEventView.getDisplayAll().addValueChangeHandler(event -> {
            boolean displayAll = !viewEventView.getDisplayAll().getValue();
            displayAll(displayAll);
            viewEventView.getDisplayAll().setValue(displayAll);
        }));

        handlers.add(viewEventView.getDisplayAllSelections().addValueChangeHandler(event -> {
            boolean displayAllSelections = !viewEventView.getDisplayAllSelections().getValue();
            displayAllSelections(displayAllSelections);
        }));

        handlers.add(viewEventView.getZoomOrder().addClickHandler(event -> {
            if(eventDTO != null) {
                zoomProducts(eventDTO.getProductsOrdered());
            }
        }));

        handlers.add(viewEventView.getDownloadOrder().addClickHandler(event -> {
            viewEventView.displayLoadingPage("Uploading products to local server...");
            CustomerService.App.getInstance().uploadOrder(eventDTO.getId(), new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable caught) {
                    viewEventView.hideWindowLoading();
                    Window.alert("Failed to upload order, reason is: " + caught.getMessage());
                }

                @Override
                public void onSuccess(String message) {
                    viewEventView.hideWindowLoading();
                    Window.alert(message);
                }
            });
        }));

        activityEventBus.addHandler(CreditUpdatedEvent.TYPE, event -> {
            viewEventView.setAvailableFunds(new Price(event.getCreditUpdatedNotification().amount, event.getCreditUpdatedNotification().currency));
        });

        handlers.add(viewEventView.getMakePayment().addClickHandler(event -> {
            if(!viewEventView.hasAcceptedTerms()) {
                Window.alert("Please accept our sales conditions first");
                return;
            }
            viewEventView.displayLoadingPage("Requesting payment...");
            HashMap<String, Price> productsPayment = new HashMap<>();
            for (ProductOrderDTO productOrderDTO : eventDTO.getProductsOrdered()) {
                if (productOrderDTO.getStatus() == PRODUCTORDER_STATUS.Quoted) {
                    productsPayment.put(productOrderDTO.getId(), productOrderDTO.getConvertedOfferedPrice());
                }
            }
            CustomerService.App.getInstance().acceptAndMakeProductOrdersPayment(eventDTO.getId(), productsPayment, new AsyncCallback<PaymentTransactionDTO>() {
                @Override
                public void onFailure(Throwable caught) {
                    viewEventView.hideWindowLoading();
                    Window.alert("Could not pay for products, message is " + caught.getMessage());
                }

                @Override
                public void onSuccess(PaymentTransactionDTO result) {
                    viewEventView.hideWindowLoading();
                    loadEvent(eventDTO.getId());
                }
            });
        }));

    }

    private void displayAll(boolean display) {
        if(eventDTO != null) {
            for (ProductOrderDTO productOrderDTO : eventDTO.getProductsOrdered()) {
                productOrderDTO.setVisible(display);
                viewEventView.updateProductOrderDisplay(productOrderDTO);
            }
        }
        viewEventView.getDisplayAll().setValue(display);
    }

    private void displayAllSelections(boolean display) {
        if(eventDTO != null) {
            for (ProductOrderDTO productOrderDTO : eventDTO.getProductsOrdered()) {
                if(productOrderDTO.getAOI() != null) {
                    productOrderDTO.getAOI().setVisible(display);
                }
            }
        }
        viewEventView.getDisplayAllSelections().setValue(display);
    }

    private void setCurrentTime(Date currentTime) {
        viewEventView.setCurrentTime(currentTime);
    }

    @Override
    protected void handleOrderNotification(ProductOrderNotification productOrderNotification) {
        printLog("Received order notification " + productOrderNotification);
        boolean sameOrder = productOrderNotification.eventId.contentEquals(eventDTO.getId());
        // default is to display a message
        printLog("Status is " + productOrderNotification.status);
        switch(productOrderNotification.status) {
            case Completed: {
                String message = "Product order " + productOrderNotification.productId + " available";
                if(productOrderNotification.publishStatus != null) {
                    switch (productOrderNotification.publishStatus) {
                        case Publishing: {
                            message += ", publishing started";
                        }
                        break;
                        case Failed: {
                            message += ", publishing failed";
                        }
                        break;
                        case Published: {
                            message += ", published!";
                        }
                    }
                }
                HTMLPanel panel = new HTMLPanel(message +
                    (sameOrder ? "" :
                    " Click <a style='color: white; text-decoration: underline;' href='#" + PlaceHistoryHelper.convertPlace(
                            new ViewEventPlace(Utils.generateTokens(ViewEventPlace.TOKENS.eventid.toString(), productOrderNotification.eventId))) +
                    "'>here</a> to view product"));
                panel.getElement().getStyle().setPadding(20, Style.Unit.PX);
                panel.getElement().getStyle().setBackgroundColor("green");
                panel.getElement().getStyle().setColor("white");
                Toast.getInstance().display(panel, 30000);
            } break;
            case Failed: {
                HTMLPanel panel = new HTMLPanel("Product order " + productOrderNotification.productId + " has failed." +
                        (sameOrder ? "" :
                        "click <a style='color: white; text-decoration: underline;' href='#" + PlaceHistoryHelper.convertPlace(
                        new ViewEventPlace(Utils.generateTokens(ViewEventPlace.TOKENS.eventid.toString(), productOrderNotification.eventId))) +
                        "'>here</a> to view product."));
                panel.getElement().getStyle().setPadding(20, Style.Unit.PX);
                panel.getElement().getStyle().setBackgroundColor("red");
                panel.getElement().getStyle().setColor("white");
                Toast.getInstance().display(panel, 30000);
            } break;
        }
        // refresh the product order display
        if(sameOrder) {
            // TODO - update status and values for product order
            // find product order
            ProductOrderDTO productOrder = ListUtil.findValue(eventDTO.getProductsOrdered(), new ListUtil.CheckValue<ProductOrderDTO>() {
                @Override
                public boolean isValue(ProductOrderDTO value) {
                    return value.getId().equals(productOrderNotification.productId);
                }
            });
            // check if we have a change from Documentation to Quoted or DocumentationProvided
            boolean statusChanged = productOrder.getStatus() != productOrderNotification.status;
            boolean needsUpdating = statusChanged && ListUtil.toList(new PRODUCTORDER_STATUS[] {PRODUCTORDER_STATUS.Documentation, PRODUCTORDER_STATUS.DocumentationProvided, PRODUCTORDER_STATUS.Quoted, PRODUCTORDER_STATUS.Cancelled, PRODUCTORDER_STATUS.ChangeRequested}).contains(productOrderNotification.status);
            productOrder.setStatus(productOrderNotification.status);
            productOrder.setPublicationStatus(productOrderNotification.publishStatus);
            productOrder.setThumbnailURL(productOrderNotification.thumbnailUrl);
            productOrder.setProductWMSServiceURL(productOrderNotification.productServiceWMSUrl);
            productOrder.setPublishedProducts(productOrderNotification.publishedProducts);
            viewEventView.updateProductOrderPublishStatuses(productOrder);
            if(productOrderNotification.publishedProducts != null) {
                viewEventView.updateProductOrderThumbnail(productOrder);
                viewEventView.updateProductOrderPublishMap(productOrder);
            }
            viewEventView.scrollIntoView(productOrder);
            if(needsUpdating) {
                // update the license requirement display or the payment display
                // needed as it needs to reload the eulas requests from the server
                if(reloadTimer == null) {
                    reloadTimer = new Timer() {

                        @Override
                        public void run() {
                            reloadTimer = null;
                            loadEvent(eventDTO.getId());
                        }
                    };
                    reloadTimer.schedule(1000);
                }
            }
        }

    }

    @Override
    public void baseMapChanged(String mapId) {
        SettingsHelper.setBaseMapId(mapId);
    }

    private void changeDisplaySettings(double transparencyOverlays, double productSelectionOpacity) {
        SettingsHelper.setProductSelectionOpacity(productSelectionOpacity);
        SettingsHelper.setOverlayTransparency(transparencyOverlays);
        viewEventView.setMapPanelDisplaySettings(SettingsHelper.getOverlayTransparency(), SettingsHelper.getProductSelectionOpacity());
    }

    static private native void printLog(String message) /*-{
        $wnd['console'].log(message);
    }-*/;

}
