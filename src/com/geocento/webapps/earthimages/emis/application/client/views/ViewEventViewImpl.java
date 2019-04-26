package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.application.client.widgets.MenuArrowedPanel;
import com.geocento.webapps.earthimages.emis.application.client.widgets.TimeGrid;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOIRectangle;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.application.client.event.MapLibraryChanged;
import com.geocento.webapps.earthimages.emis.application.client.place.EventsPlace;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.utils.UserLayersHelper;
import com.geocento.webapps.earthimages.emis.application.client.widgets.*;
import com.geocento.webapps.earthimages.emis.application.share.EULARequest;
import com.geocento.webapps.earthimages.emis.application.share.Extent;
import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.application.share.WorkspaceSummaryDTO;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.Comment;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.property.domain.ChoiceProperty;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.property.domain.TextProperty;
import com.metaaps.webapps.libraries.client.widget.*;
import com.metaaps.webapps.libraries.client.widget.util.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 26/09/2014.
 */
public class ViewEventViewImpl extends Composite implements ViewEventView, ResizeHandler {

    interface OrderingViewImplUiBinder extends UiBinder<Widget, ViewEventViewImpl> {
    }

    private static OrderingViewImplUiBinder ourUiBinder = GWT.create(OrderingViewImplUiBinder.class);

    static public interface Style extends CssResource {

        String producOrder();

        String error();

        String tickItem();

        String grow();

        String packagePanel();

        String mapPanel();
    }

    @UiField
    Style style;

    @UiField(provided = true)
    OrderTemplateViewImpl templateView;
    @UiField
    MessageLabel message;
    @UiField
    Label comment;
    @UiField
    SwitchWidget switchPanels;
    @UiField
    HTMLPanel content;
    @UiField
    HTMLPanel passwordPanel;
    @UiField
    PasswordTextBox password;
    @UiField
    HTMLPanel submitPasswordPanel;
    @UiField
    Anchor viewOrders;
    @UiField
    HTMLPanel comments;
    @UiField
    HTMLPanel commentPanel;
    @UiField
    TimeGrid timeGrid;
    @UiField(provided = true)
    OrderStatusWidget orderStatusWidget;
    @UiField
    HTMLPanel defaultPackage;
    @UiField
    HTMLPanel packageLists;
    @UiField
    IconAnchor addPackage;
    @UiField
    IconLabel zoomOrder;
    @UiField
    IconLabel displayAll;
    @UiField
    IconLabel downloadProducts;
    @UiField
    MenuArrowedPanel actions;
    @UiField
    HTMLPanel timeGridPanel;
    @UiField
    HTMLPanel licensesRequired;
    @UiField
    HTMLPanel paymentsPanel;
    @UiField
    Label paymentMessage;
    @UiField
    Label prepaidValue;
    @UiField
    Anchor payPrepaid;
    @UiField
    Anchor addFunds;
    @UiField
    CheckBox acceptTerms;
    @UiField
    AnchorElement termsOfSales;
    @UiField
    HTMLPanel makePaymentPanel;
    @UiField
    IconLabel displayAllSelections;

    private MapOrderPanel mapPanel;

    private StyleResources styleResources = StyleResources.INSTANCE;

    private Presenter presenter;

    private boolean allDisplayed;

    public boolean allDisplayedSelections;

    public ViewEventViewImpl(final ClientFactory clientFactory) {

        templateView = new OrderTemplateViewImpl(clientFactory);

        orderStatusWidget = new OrderStatusWidget();

        initWidget(ourUiBinder.createAndBindUi(this));

        templateView.setStep(OrderTemplateViewImpl.ORDER_STEP.download);

        orderStatusWidget.setStatusChangeHandler(new ValueChangeHandler<ORDER_STATUS>() {
            @Override
            public void onValueChanged(ORDER_STATUS value) {
                //Window.alert("Changed to " + value);
                if(value == ORDER_STATUS.ARCHIVED)
                    presenter.archiveEvent();
            }
        });

        // add an arrow
        Element arrow = DOM.createSpan();
        arrow.addClassName("ei-arrowDown");
        arrow.addClassName("black");
        arrow.getStyle().setMarginLeft(5, com.google.gwt.dom.client.Style.Unit.PX);
        DOM.appendChild(actions.getElement(), arrow);

        mapPanel = new MapOrderPanel();
        mapPanel.setPresenter(new MapOrderPanel.Presenter() {
            @Override
            public void onProductOrderClicked(ProductOrderDTO productOrder, EOLatLng eoLatLng) {

            }

            @Override
            public void onProductOrderChanged(ProductOrderDTO product) {

            }
        });
        templateView.getMapContainer().add(timeGridPanel);
        mapPanel.getElement().getStyle().setPosition(com.google.gwt.dom.client.Style.Position.ABSOLUTE);
        mapPanel.getElement().getStyle().setTop(22, com.google.gwt.dom.client.Style.Unit.PX);
        mapPanel.getElement().getStyle().setBottom(10, com.google.gwt.dom.client.Style.Unit.PX);
        mapPanel.getElement().getStyle().clearHeight();
        clientFactory.getEventBus().addHandler(MapLibraryChanged.TYPE, event -> {
            mapPanel.getElement().getStyle().clearHeight();
            mapPanel.getElement().getStyle().setPosition(com.google.gwt.dom.client.Style.Position.ABSOLUTE);
        });
        templateView.setMapPanel(mapPanel, 750, false);

        IconAnchor wcsSelection = new IconAnchor();
        wcsSelection.setSimple(true);
        wcsSelection.setResource(styleResources.wcsDownload());
        wcsSelection.getElement().getStyle().setMarginLeft(5, com.google.gwt.dom.client.Style.Unit.PX);
        wcsSelection.setTooltip("Click to select region for image download");
        templateView.mapTools.add(wcsSelection);
        wcsSelection.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AOIRectangle aoiRectangle = new AOIRectangle();
                aoiRectangle.setStrokeColor("33ff33");
                aoiRectangle.setStrokeThickness(1);
                aoiRectangle.setFillOpacity(0.1);
                aoiRectangle.setFillColor("33ff33");
                mapPanel.drawNewFeature(aoiRectangle, new AsyncCallback<AOI>() {
                    @Override
                    public void onFailure(Throwable caught) {

                    }

                    @Override
                    public void onSuccess(AOI result) {
                        presenter.handleWCSRequest(((AOIRectangle) result).getBounds());
                    }
                });
            }
        });

        viewOrders.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent clickEvent) {
                Application.clientFactory.getPlaceController().goTo(new EventsPlace());
            }

        });

        termsOfSales.setHref(Application.getApplicationSettings().getTermsOfSalesUrl());
        addFunds.setHref("#" + PlaceHistoryHelper.convertPlace(new SettingsPlace(Utils.generateTokens(SettingsPlace.TOKENS.tab.toString(), SettingsPlace.TOKENS.transactions.toString()))));

        clearAll();

        LoadingPanel.getInstance().getElement().getStyle().setZIndex(1000);

        Window.addResizeHandler(this);

    }

    @Override
    public void setMapBounds(EOBounds bounds) {
        mapPanel.setEOBounds(bounds);
    }

    @Override
    public void setBaseMapId(String baseMapId) {

    }

    @Override
    public void addProductOrder(final ProductOrderDTO productOrder) {
        String packageName = productOrder.getLabel();
        HasWidgets packagePanel = findPackagePanel(packageName);
        if(packagePanel == null) {
            packagePanel = addPackagePanel(productOrder.getLabel());
        }
        final ProductOrderWidget productOrderWidget = new ProductOrderWidget(productOrder);
        productOrderWidget.addStyleName(style.producOrder());
        productOrderWidget.setProductChangeHandlers(new ProductOrderWidget.ProductChangeHandlers() {

            @Override
            public void handleRemove() {
                presenter.deleteProduct(productOrder.getId());
            }

            @Override
            public void toggleShow() {
                boolean isVisible = productOrder.isVisible();
                // toggle map display
                setProductOrderVisible(productOrder, productOrderWidget, !isVisible);
                // force refresh of z-indexes
                updateMapProductOrders();
            }

            @Override
            public void handleZoom() {
                mapPanel.setEOBounds(EOBounds.getBounds(productOrder.getCoordinates()));
                Scheduler.get().scheduleDeferred(() -> {
                    Element element = productOrderWidget.getElement();
                    showArrow(false, element, productOrder);
                });
            }

            @Override
            public void toggleHighlighted() {
                highlightProduct(productOrderWidget, !productOrderWidget.isHighlighted());
            }

            @Override
            public void activateClipping(boolean selected) {
                mapPanel.activateClipping(productOrder, selected);
            }

            @Override
            public void activateWCSDownload(boolean selected) {
                mapPanel.activateWCSSelection(productOrder, selected);
            }

            @Override
            public void requestWCS(String format) {
                EOBounds bounds = mapPanel.getWCSBounds();
                if(bounds == null) {
                    return;
                }
                EOBounds productBounds = EOBounds.getBounds(productOrder.getCoordinates());
                if(!bounds.intersects(productBounds)) {
                    Window.alert("Your area selection is outside the product");
                    return;
                }
                if(!productBounds.contains(bounds)) {
                    if(!Window.confirm("Your area selection is not fully inside the product, continue?")) {
                        return;
                    }
                }
                Extent extent = new Extent();
                extent.setSouth(bounds.getCoordinatesSW().getLat());
                extent.setWest(bounds.getCoordinatesSW().getLng());
                extent.setNorth(bounds.getCoordinatesNE().getLat());
                extent.setEast(bounds.getCoordinatesNE().getLng());
                String extentString = ((Extent.ExtentMapper) GWT.create(Extent.ExtentMapper.class)).write(extent);
                Window.open("./api/download-product/download/" + productOrder.getId() + "/selection?format="
                        + format + "&extent=" + extentString, "_blank", null);
            }

        });

        // add arrow on hovering
        addArrow(false, productOrderWidget, productOrder);

        packagePanel.add(productOrderWidget);

        // add to time line
        HTMLPanel tickPanel = new HTMLPanel("");
        tickPanel.addStyleName(style.tickItem());
        tickPanel.addStyleName(style.grow());
        Tooltip.getTooltip().registerTooltip(tickPanel, productOrder.getDescription());
        timeGrid.addTick(tickPanel, productOrder.getOriginalProductAcquisitionTime(), 11);
        // add arrow on hovering
        addArrow(true, tickPanel, productOrder);
        DOM.sinkEvents(tickPanel.getElement(), Event.getTypeInt(ClickEvent.getType().getName()) | Event.getTypeInt(MouseOverEvent.getType().getName()) | Event.getTypeInt(MouseOutEvent.getType().getName()));
        tickPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                highlightProduct(productOrderWidget, !productOrderWidget.isHighlighted());
                // zoom in map and scroll into view the widget
                mapPanel.setEOBounds(EOBounds.getBounds(productOrder.getCoordinates()));
                productOrderWidget.getElement().scrollIntoView();
            }
        }, ClickEvent.getType());
        // initialise the product observation display parameters
        AOI aoi = productOrder.getAOI();
        mapPanel.addFeature(aoi);
        mapPanel.addProductOrder(productOrder);
        setProductOrderVisible(productOrder, productOrderWidget, productOrder.isVisible());
        updateMapProductOrders();
    }

    private void addArrow(boolean topPosition, Widget widget, ProductOrderDTO productOrder) {
        Element element = widget.getElement();
        DOM.sinkEvents(element, Event.getTypeInt(MouseOverEvent.getType().getName()));
        DOM.sinkEvents(element, Event.getTypeInt(MouseOutEvent.getType().getName()));
        widget.addDomHandler(event -> showArrow(topPosition, element, productOrder), MouseOverEvent.getType());
        widget.addDomHandler(event -> templateView.hideArrow(), MouseOutEvent.getType());
    }

    private HasWidgets findPackagePanel(String packageName) {
        if (packageName == null) {
            return defaultPackage;
        }
        // find package
        List<Widget> packageWidgets = WidgetUtil.getWidgets(packageLists, new WidgetUtil.CheckValue() {
            @Override
            public boolean isValue(Widget widget) {
                return widget instanceof PackageWidget && ((PackageWidget) widget).getPackageName().equals(packageName);
            }
        });
        if (packageWidgets.size() > 0) {
            return (PackageWidget) packageWidgets.get(0);
        }
        return null;
    }

    private HasWidgets addPackagePanel(String packageName) {
        // create a new one if we couldn't find any existing one
        PackageWidget packageWidget = new PackageWidget();
        packageWidget.setPackageName(packageName);
        packageWidget.addStyleName(style.packagePanel());
        packageWidget.setCompletionHandler(new CompletionHandler<String>() {
            @Override
            public void onCompleted(String result) throws ValidationException {
                presenter.changePackageName(packageName, result);
                packageWidget.setPackageName(result);
            }

            @Override
            public void onCancel() {

            }
        });
        packageWidget.setDraggableInto();
        packageLists.add(packageWidget);
        return packageWidget;
    }

    @Override
    public void setTimeGridTimeFrame(Date minDate, Date maxDate) {
        timeGrid.setTimeFrame(minDate, maxDate);
    }

    protected void setProductOrderVisible(ProductOrderDTO productOrderDTO, ProductOrderWidget productOrderWidget, boolean visible) {
        // toggle icon display
        productOrderWidget.displayShow(visible);
        productOrderWidget.displayZoom(visible);
        if(!visible) {
            highlightProduct(productOrderWidget, false);
        }
        productOrderDTO.setVisible(visible);
        // toggle map display
        mapPanel.updateDisplay();
    }

    private void highlightProduct(ProductOrderWidget productOrderWidgetHighlight, boolean highlighted) {
        for(ProductOrderWidget productOrderWidget : getProductOrderWidgets()) {
            productOrderWidget.setHighlighted(false);
        }
        productOrderWidgetHighlight.setHighlighted(highlighted);
        mapPanel.highlightProductOrder(highlighted ? productOrderWidgetHighlight.getProductOrder() : null);
        updateMapPanelControls();
    }

    private void updateMapPanelControls() {
    }

    @Override
    public void displayPayment(boolean display) {
        paymentsPanel.setVisible(display);
    }

    @Override
    public void displayPaymentMessage(String message) {
        paymentMessage.setText(message);
    }

    @Override
    public void setAvailableFunds(Price price) {
        if(price == null) {
            prepaidValue.setText("You have no money on your pre paid account");
        } else {
            prepaidValue.setText("You have currently " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(price) + " left on your pre paid account");
        }
    }

    @Override
    public void displayPayPrepaid(boolean display) {
        payPrepaid.setVisible(display);
        makePaymentPanel.setVisible(display);
        addFunds.setVisible(!display);
    }

    @Override
    public HasClickHandlers getMakePayment() {
        return payPrepaid;
    }

    @Override
    public boolean hasAcceptedTerms() {
        return acceptTerms.getValue();
    }

    @UiHandler("addPackage")
    void addPackage(ClickEvent clickEvent) {
        PopupPropertyEditor.getInstance().edit("Create new package",
                new TextProperty("Name of package", null, "", true, 3, 100),
                new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        addPackagePanel(((String) result.get(0).getValue()));
                    }

                    @Override
                    public void onCancel() {

                    }
                }
        );
    }

    private List<ProductOrderWidget> getProductOrderWidgets() {
        List<ProductOrderWidget> productOrderWidgets = new ArrayList<ProductOrderWidget>();
        productOrderWidgets.addAll(WidgetUtil.getWidgets(defaultPackage, new WidgetUtil.CheckValue() {
            @Override
            public boolean isValue(Widget widget) {
                return widget instanceof ProductOrderWidget;
            }
        }));
        for(Widget widget : packageLists) {
            if(widget instanceof PackageWidget) {
                productOrderWidgets.addAll(WidgetUtil.getWidgets(((PackageWidget) widget), new WidgetUtil.CheckValue() {
                    @Override
                    public boolean isValue(Widget widget) {
                        return widget instanceof ProductOrderWidget;
                    }
                }));
            }
        }
        return productOrderWidgets;
    }

    @Override
    public void displayMessageLoading(String message) {
        templateView.displayPageLoading(message);
    }

    @Override
    public void displayMessage(String message) {
        this.message.displayMessage(message);
    }

    @Override
    public void displayComment(String message) {
        comment.setVisible(true);
        comment.setText(message);
    }

    @Override
    public void displayEventContent() {
        templateView.displayContent();
        switchPanels.showWidget(content);
    }

    @Override
    public void requestPassword(final AsyncCallback<String> asyncCallback) {
        templateView.displayContent();
        switchPanels.showWidget(passwordPanel);
        password.setText("");
        submitPasswordPanel.clear();
        Anchor submitPassword = new Anchor("Submit");
        submitPassword.addStyleName(StyleResources.INSTANCE.style().actionAnchorButton());
        submitPassword.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                asyncCallback.onSuccess(password.getText());
            }
        });
        submitPasswordPanel.add(submitPassword);
    }

    @Override
    public void setOrderStatus(ORDER_STATUS status) {
        orderStatusWidget.setVisible(false); //ListUtil.toList(new ORDER_STATUS[] {ORDER_STATUS.COMPLETED, ORDER_STATUS.ARCHIVED}).contains(status));
        orderStatusWidget.setStatus(status);
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
    public List<ProductOrderDTO> getProductOrders() {
        return ListUtil.mutate(getProductOrderWidgets(), new ListUtil.Mutate<ProductOrderWidget, ProductOrderDTO>() {
            @Override
            public ProductOrderDTO mutate(ProductOrderWidget productOrderWidget) {
                return productOrderWidget.getProductOrder();
            }
        });
    }

    @Override
    public void deleteProductOrder(String id) {

        ProductOrderWidget productOrderWidget = ListUtil.findValue(getProductOrderWidgets(), new ListUtil.CheckValue<ProductOrderWidget>()
        {
            @Override
            public boolean isValue(ProductOrderWidget value)
            {
                return value.getProductOrder().getId().equals(id);
            }
        });

        HasWidgets packagePanel = findPackagePanel(productOrderWidget.getProductOrder().getLabel());
        packagePanel.remove(productOrderWidget);
        mapPanel.removeFeature(productOrderWidget.getProductOrder().getAOI());
        mapPanel.removeProductOrder(productOrderWidget.getProductOrder());
        mapPanel.updateDisplay();
    }

    @Override
    public void clearAll() {
        timeGrid.clearAll();
        packageLists.clear();
        defaultPackage.clear();
        mapPanel.clearProductOrders();
        mapPanel.cleanUp();
        acceptTerms.setValue(false);
    }

    @Override
    public void setAoI(AOI aoi, boolean editable) {

    }

    private void setError(TextBox textBox, boolean error) {
        textBox.setStyleName(style.error(), error);
    }

    @Override
    public void displayLoadingProductOrders(String message) {
        this.message.displayLoading(message);
    }

    @Override
    public void hideLoadingProductOrders() {
        this.message.setVisible(false);
    }

    @Override
    public void displayResultErrorMessage(String message) {
        this.message.displayError(message);
    }

    @Override
    public void displayLoadingPage(String message) {
        templateView.displayWindowLoading(message);
    }

    @Override
    public void hideWindowLoading() {
        templateView.hideWindowLoading();
    }

    @Override
    public void displayWindowErrorMessage(String message) {
        templateView.displayWindowErrorMessage(message);
    }

    @Override
    public void displayPageLoadingError(String message) {
        templateView.displayPageLoadingError(message);
    }

    @Override
    public void setComments(List<Comment> comments) {
        boolean hasComments = comments != null && comments.size() > 0;
        commentPanel.setVisible(hasComments);
        if(hasComments) {
            for(Comment comment : comments) {
                //this.comments.add(new CommentWid);
            }
        }
    }

    @Override
    public void updateProductOrderPublishStatuses(ProductOrderDTO productOrder) {
        // find widget
        ProductOrderWidget productOrderWidget = getProductOrderWidget(productOrder);
        if(productOrderWidget == null) {
            return;
        }
        productOrderWidget.updateStatuses();
    }

    private ProductOrderWidget getProductOrderWidget(ProductOrderDTO productOrder) {
        HasWidgets packagePanel = findPackagePanel(productOrder.getLabel());
        return WidgetUtil.getWidget(packagePanel, new WidgetUtil.CheckValue() {
            @Override
            public boolean isValue(Widget widget) {
                return widget instanceof ProductOrderWidget && ((ProductOrderWidget) widget).getProductOrder().getId().equals(productOrder.getId());
            }
        });
    }

    @Override
    public void scrollIntoView(ProductOrderDTO productOrder) {
        ProductOrderWidget productOrderWidget = getProductOrderWidget(productOrder);
        if(productOrderWidget == null) {
            return;
        }
        productOrderWidget.getElement().scrollIntoView();
    }

    @Override
    public void updateProductOrderPublishMap(ProductOrderDTO productOrder) {
        mapPanel.removeProductOrder(productOrder);
        mapPanel.addProductOrder(productOrder);
        mapPanel.updateDisplay();
    }

    @Override
    public void moveProductOrderBelow(ProductOrderDTO targetProductOrderDTO, ProductOrderDTO productOrderDTO) {
        ProductOrderWidget targetProductOrderWidget = getProductOrderWidget(targetProductOrderDTO);
        ProductOrderWidget productOrderWidget = getProductOrderWidget(productOrderDTO);
        HasWidgets packagePanel = findPackagePanel(targetProductOrderDTO.getLabel());
        List<Widget> widgets = WidgetUtil.getWidgets(packagePanel, new WidgetUtil.CheckValue() {
            @Override
            public boolean isValue(Widget widget) {
                return widget instanceof ProductOrderWidget;
            }
        });
        widgets.remove(productOrderWidget);
        widgets.add(widgets.indexOf(targetProductOrderWidget) + 1, productOrderWidget);
        packagePanel.clear();
        for(Widget widget : widgets) {
            packagePanel.add(widget);
        }
        // update map display
        updateMapProductOrders();
    }

    private void updateMapProductOrders() {
        mapPanel.clearProductOrders();
        for(ProductOrderWidget productOrderWidget : getProductOrderWidgets()) {
            if(productOrderWidget.isVisible()) {
                mapPanel.addProductOrder(productOrderWidget.getProductOrder());
            }
        }
    }

    @Override
    public void moveProductOrder(String packageName, ProductOrderDTO productOrderDTO) {
        ProductOrderWidget productOrderWidget = getProductOrderWidget(productOrderDTO);
        findPackagePanel(packageName).add(productOrderWidget);
    }

    @Override
    public void displayAddProductOrderWorkspace(ProductOrderDTO productOrder, List<WorkspaceSummaryDTO> workspaces) {
        final WorkspacePopupPropertyEditor popup = WorkspacePopupPropertyEditor.getInstance();
        boolean hasExistingOrders = workspaces.size() == 0;
        String title = hasExistingOrders ? "Create new workspace" : "Add to workspace / create new workspace";
        popup.editAt(null, Util.TYPE.right, title,
                "You are about to create a new workspace, please provide the information below",
                "Please select the workspace where the product will be added",
                workspaces,
                new WorkspacePopupPropertyEditor.Presenter() {
                    @Override
                    public void onCancel() {
                        popup.hide();
                    }

                    @Override
                    public void createNewWorkspace(String name, String additionalInformation) {
                        popup.hide();
                        presenter.createWorkspaceProduct(name, additionalInformation, productOrder);
                    }

                    @Override
                    public void addToExistingWorkspace(String workspaceId) {
                        presenter.addWorkspaceProduct(workspaceId, productOrder);
                    }
                });
    }

    @Override
    public void updateProductOrderWorkspaces(ProductOrderDTO productOrder) {
        // find widget
        ProductOrderWidget productOrderWidget = getProductOrderWidget(productOrder);
        if(productOrderWidget == null) {
            return;
        }
        productOrderWidget.updateWorkspaces();
    }

    private void showArrow(boolean topPosition, final Element element, final ProductOrderDTO productOrderDTO) {
        if(productOrderDTO == null) {
            mapPanel.outlineProduct(null);
            return;
        }
        EOBounds productBounds = EOBounds.getBounds(productOrderDTO.getCoordinates());
        templateView.showArrow(topPosition, element, productBounds);
    }

    @Override
    public HasClickHandlers getZoomOrder() {
        return zoomOrder;
    }

    @Override
    public HasClickHandlers getDownloadOrder() {
        return downloadProducts;
    }

    @Override
    public void updateProductOrderDisplay(ProductOrderDTO productOrder) {
        ProductOrderWidget productOrderWidget = getProductOrderWidget(productOrder);
        setProductOrderVisible(productOrder, productOrderWidget, productOrder.isVisible());
    }

/*
    @Override
    public boolean isDisplayedAll() {
        return allDisplayed;
    }

    @Override
    public void setDisplayedAll(boolean displayedAll) {
        displayAll.getImage().getElement().getStyle().setOpacity(displayedAll ? 1.0 : 0.3);
        allDisplayed = displayedAll;
    }
*/

    @Override
    public HasValue<Boolean> getDisplayAll() {
        return new HasValue<Boolean>() {
            @Override
            public void fireEvent(GwtEvent<?> event) {
                displayAll.fireEvent(event);
            }

            @Override
            public HandlerRegistration addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler<Boolean> handler) {
                displayAll.addClickHandler(event -> ValueChangeEvent.fire(this, allDisplayed));
                return displayAll.addHandler(handler, ValueChangeEvent.getType());
            }

            @Override
            public Boolean getValue() {
                return allDisplayed;
            }

            @Override
            public void setValue(Boolean value) {
                displayAll.getImage().getElement().getStyle().setOpacity(value ? 1.0 : 0.3);
                allDisplayed = value;
            }

            @Override
            public void setValue(Boolean value, boolean fireEvents) {
                setValue(value);
                if (fireEvents) {
                    ValueChangeEvent.fire(this, value);
                }
            }
        };
    }

    @Override
    public HasValue<Boolean> getDisplayAllSelections() {
        return new HasValue<Boolean>() {

            @Override
            public void fireEvent(GwtEvent<?> event) {
                displayAllSelections.fireEvent(event);
            }

            @Override
            public HandlerRegistration addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler<Boolean> handler) {
                displayAllSelections.addClickHandler(event -> ValueChangeEvent.fire(this, allDisplayed));
                return displayAllSelections.addHandler(handler, ValueChangeEvent.getType());
            }

            @Override
            public Boolean getValue() {
                return allDisplayedSelections;
            }

            @Override
            public void setValue(Boolean value) {
                displayAllSelections.getImage().getElement().getStyle().setOpacity(value ? 1.0 : 0.3);
                allDisplayedSelections = value;
                mapPanel.updateDisplay();
            }

            @Override
            public void setValue(Boolean value, boolean fireEvents) {
                setValue(value);
                if (fireEvents) {
                    ValueChangeEvent.fire(this, value);
                }
            }
        };
    }

    @Override
    public void getWCSFormat(String title, String message, AsyncCallback<String> callback) {
        PopupPropertyEditor.getInstance().edit(title, message, ListUtil.toList(new Property[]{
                new ChoiceProperty("Select a format", null, null, true, true, new String[]{"png", "tiff", "geotiff"})
        }), new CompletionHandler<List<Property>>() {
            @Override
            public void onCompleted(List<Property> result) throws ValidationException {
                PopupPropertyEditor.getInstance().hide();
                callback.onSuccess((String) result.get(0).getValue());
            }

            @Override
            public void onCancel() {
                PopupPropertyEditor.getInstance().hide();
                callback.onFailure(null);
            }
        });
    }

    @Override
    public void hideLicensesRequired() {
        licensesRequired.setVisible(false);
    }

    @Override
    public void showLicensesRequired(List<EULARequest> policiesToSign) {
        licensesRequired.setVisible(true);
        licensesRequired.clear();
        licensesRequired.add(new HTML("<p>The following user licenses need signing: " +
                ListUtil.toString(policiesToSign, value -> {
                    String eulaName = value.getEulaName();
                    List<String> tokens = ListUtil.toList(new String[] {LicensingPlace.TOKENS.eulaDocumentId.toString(), value.getEULADocumentId() + ""});
                    String eiOrderId = value.getEiOrderId();
                    if(!StringUtils.isEmpty(eiOrderId)) {
                        tokens.addAll(ListUtil.toList(new String[] {LicensingPlace.TOKENS.eiOrderId.toString(), eiOrderId + ""}));
                    }
                    return "<a href='#" + PlaceHistoryHelper.convertPlace(
                            new LicensingPlace(Utils.generateTokens(tokens.toArray(new String[0])))) + "' target='_blank'>" + eulaName + "</a>";
                }, ", ") +
                "</p>"
        ));
    }

    @Override
    public void updateProductOrderThumbnail(ProductOrderDTO productOrder) {
        // find widget
        ProductOrderWidget productOrderWidget = getProductOrderWidget(productOrder);
        if(productOrderWidget == null) {
            return;
        }
        productOrderWidget.updateThumbnail();
    }

    @Override
    public void setCurrentTime(Date currentTime) {
        // update time layers if any
        UserLayersHelper.setLayersTime(currentTime);
        mapPanel.updateLayers();
    }

    @Override
    public void onResize(ResizeEvent event) {

    }

}
