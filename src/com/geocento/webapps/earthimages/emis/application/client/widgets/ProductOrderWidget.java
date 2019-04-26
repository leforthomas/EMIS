package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.common.client.places.CustomHistorian;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PUBLICATION_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.ExpandWidget;
import com.metaaps.webapps.libraries.client.widget.MenuArrowedPanel;
import com.metaaps.webapps.libraries.client.widget.*;
import com.metaaps.webapps.libraries.client.widget.util.*;

import java.util.ArrayList;
import java.util.List;

public class ProductOrderWidget extends ProductBaseWidget {

    static public interface ProductChangeHandlers extends BaseProductChangeHandlers {

        void activateClipping(boolean selected);

        void activateWCSDownload(boolean selected);

        void requestWCS(String format);

/*
        void onMouseOver(Element element, ProductDisplay value);

        void onMouseOut(Element parent, ProductDisplay value);
*/
    }

    private final IconLabel estimatedDeliveryDate;
    private final IconAnchor downloadingIcon;
    private final IconAnchor downloadIcon;
    private final IconAnchor publishDownloadIcon;
    private final HTMLPanel dashboardSelection;
    private final IconLabel processing;
    private final ProductOrderStatusWidget status;

    private HorizontalPanel bottomActionsPanel;

    protected ProductOrderDTO productOrder;

    private ProductChangeHandlers productChangeHandlers;

    public ProductOrderWidget(ProductOrderDTO productOrder) {
		
		super();
		
		this.productOrder = productOrder;

        // set the header widgets to the product values
        displayProductTitle(productOrder.getDescription() + " image");

        String thumbnailUrl = productOrder.getThumbnailURL();
        thumbnailImage.addLoadHandler(event -> thumbnail.getElement().getStyle().setBackgroundImage("none"));
        setThumbnail(thumbnailUrl);

        // add more information
        description.displayInfo(productOrder.getInfo());

        status = new ProductOrderStatusWidget();
        status.setStatusChangeHandler(new ValueChangeHandler<PRODUCTORDER_STATUS>() {

            @Override
            public void onValueChanged(PRODUCTORDER_STATUS value) {
                // TODO - do something when status is changed
            }
        });
        status.getElement().getStyle().setFloat(com.google.gwt.dom.client.Style.Float.RIGHT);
        status.getElement().getStyle().setMarginRight(10, com.google.gwt.dom.client.Style.Unit.PX);
        footer.getElement().getStyle().setMarginBottom(10, com.google.gwt.dom.client.Style.Unit.PX);
        footer.insert(status, 0);

        // clear unused widgets
        productSelectionExplanation.clear();
        expandOrderingParameters.setVisible(false);
        // add information on selection, license, options and comments
        expandProductProperties.setText("View selection, license, options and comments");
        // use product properties grid
        productProperties.clear();
        addTableProperty(productProperties, "Selected area", Utils.formatSurface(Utils.FORMAT.SQKILOMETERS, 1, MapPanel.getPathArea(productOrder.getCoordinates())));

        // add the licensing policy options and information
        addTableProperty(productProperties, "Selected Licensing option", productOrder.getLicense());
        // add header for parameters
        {
            int index = productProperties.getRowCount();
            productProperties.resizeRows(index + 1);
            productProperties.setWidget(index, 0, new HTML("<p style='font-weight: bold; text-decoration: underline;'>Selected product options</p>"));
        }
        // now do the order parameters
        List<UserOrderParameterDTO> orderParameters = productOrder.getParameters();
        if(orderParameters != null && orderParameters.size() > 0) {
            for(UserOrderParameterDTO userOrderParameterDTO : orderParameters) {
                addTableProperty(productProperties, userOrderParameterDTO.getName(), userOrderParameterDTO.getValue());
            }
        }

        // add comments if any
        String comments = productOrder.getComments();
        if(!StringUtils.isEmpty(comments)) {
            // add header for comments
            int index = productProperties.getRowCount();
            productProperties.resizeRows(index + 1);
            productProperties.setWidget(index, 0, new HTML("<p style='font-weight: bold; text-decoration: underline;'>Comments from order desk</p>"));
            // add comments text
            addTableProperty(productProperties, "", comments);
        }

        displayRemove(true);

        estimatedDeliveryDate = new IconLabel();
        estimatedDeliveryDate.setResource(com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.calendar());
        estimatedDeliveryDate.setText("");
        estimatedDeliveryDate.getElement().getStyle().setMarginLeft(10, com.google.gwt.dom.client.Style.Unit.PX);
        addFooterIcon(estimatedDeliveryDate);

        bottomActionsPanel = new HorizontalPanel();
        bottomActionsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        bottomActionsPanel.getElement().getStyle().setClear(com.google.gwt.dom.client.Style.Clear.BOTH);
        bottomActionsPanel.setSpacing(5);
        footer.add(bottomActionsPanel);

        downloadingIcon = new IconAnchor();
        downloadingIcon.setResource(com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.loading());
        downloadingIcon.setText("Downloading");
        downloadingIcon.setTooltip("Downloading a local copy for publishing");
        addFooterIcon(downloadingIcon);

        downloadIcon = new IconAnchor();
        downloadIcon.setResource(styleResources.download());
        downloadIcon.setText("Download Original Product");
        downloadIcon.setTooltip("Download the full original ordered product");
        downloadIcon.setTarget("_blank");
        downloadIcon.setHref(CustomHistorian.getHostPageBaseURL() + "api/download-product/download/" + productOrder.getId());
        addBottomActions(downloadIcon);

        processing = new IconLabel(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.archive(), "");
        processing.addClickHandler(event -> {
            if(productOrder.getStatus() == PRODUCTORDER_STATUS.Completed) {
                switch (productOrder.getPublicationStatus()) {
                    case Published:
                    case Failed:
                        ArrowedPopUpMenu popup = ArrowedPopUpMenu.getInstance();
                        popup.clearItems();
                        popup.addMenuItem(Application.clientFactory.getEventBus(), new PublishProductOrder(productOrder), "Republish product", null, true);
                        popup.showAt(processing, Util.TYPE.right);
                        break;
                }
            }
        });
        addFooterIcon(processing);
        publishDownloadIcon = new IconAnchor();
        publishDownloadIcon.setResource(styleResources.download());
        publishDownloadIcon.setText("Processed");
        publishDownloadIcon.setTooltip("Download the processed products");
        publishDownloadIcon.setTarget("_blank");
        addBottomActions(publishDownloadIcon);

        // add dashboard selection
        dashboardSelection = new HTMLPanel("");
        IconLabel dashboardSelectionLabel = new IconLabel(StyleResources.INSTANCE.workspace(), "Workspaces ");
        dashboardSelectionLabel.getElement().getStyle().setDisplay(com.google.gwt.dom.client.Style.Display.INLINE_BLOCK);
        dashboardSelection.add(dashboardSelectionLabel);
        dashboardsSelectionWidget = new DashboardsSelectionWidget();
        dashboardsSelectionWidget.getElement().getStyle().setDisplay(com.google.gwt.dom.client.Style.Display.INLINE_BLOCK);
        dashboardsSelectionWidget.getElement().getStyle().setPaddingLeft(0, com.google.gwt.dom.client.Style.Unit.PX);
        dashboardsSelectionWidget.getElement().getStyle().setColor("blue");
        dashboardsSelectionWidget.getElement().getStyle().setCursor(com.google.gwt.dom.client.Style.Cursor.POINTER);
        dashboardsSelectionWidget.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ArrowedPopUpMenu popupMenu = ArrowedPopUpMenu.getInstance();
                popupMenu.clearItems();
                if(productOrder.getWorkspaces() != null && productOrder.getWorkspaces().size() > 0) {
                    for (WorkspaceSummaryDTO workspace : productOrder.getWorkspaces()) {
                        popupMenu.addMenuItem(() -> EINEO.clientFactory.getPlaceController().goTo(
                                new WorkspacePlace(Utils.generateTokens(WorkspacePlace.TOKENS.workspaceid.toString(), workspace.getId()))), workspace.getName(), StyleResources.INSTANCE.worldGrid(), true);
                    }
                    popupMenu.addMenuDivider();
                }
                IconLabel addToWorkspace = new IconLabel(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.addIcon(), "Add to workspace");
                addToWorkspace.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        popupMenu.hide();
                        EINEO.clientFactory.getEventBus().fireEvent(new AddProductOrderWorkspace(productOrder));
                    }
                });
                popupMenu.addMenuItem(addToWorkspace);
                popupMenu.showAt(dashboardsSelectionWidget, Util.TYPE.below);
            }
        });
        dashboardSelection.add(dashboardsSelectionWidget);
        addBottomActions(dashboardSelection);
        updateStatuses();

        // set draggable
        titleLabel.getElement().setDraggable(Element.DRAGGABLE_TRUE);
        titleLabel.addDragStartHandler(new DragStartHandler() {
            @Override
            public void onDragStart(DragStartEvent event) {
                event.setData("text", productOrder.getId());
                event.getDataTransfer().setDragImage(titleLabel.getElement(), 10, 10);
                printLog("On drag start for product id " + productOrder.getId());
            }
        });
        setDraggableInto();
    }

    private void setThumbnail(String thumbnailUrl) {
        thumbnailImage.setUrl(thumbnailUrl == null ? "./img/no-image.png" : thumbnailUrl);
    }

    private void addBottomActions(Widget widget) {
        widget.addStyleName(style.footerIcon());
        bottomActionsPanel.add(widget);
    }

    static private native void printLog(String message) /*-{
        $wnd['console'].log(message);
    }-*/;

    public void updateStatuses() {
        PRODUCTORDER_STATUS status = productOrder.getStatus();
        this.status.setStatus(productOrder.getStatus() == null ? PRODUCTORDER_STATUS.Unknown : productOrder.getStatus());
        // hide by default
        estimatedDeliveryDate.setVisible(false);
        downloadingIcon.setVisible(false);
        downloadIcon.setVisible(false);
        publishDownloadIcon.setVisible(false);
        dashboardSelection.setVisible(false);
        processing.setVisible(false);
        // set estimated price
        estimatedPrice.setVisible(false);
        switch (status) {
            case Created:
            case Documentation:
            case DocumentationProvided:
            case Submitted:
            case Quoted: {
                // display price
                Price totalPrice = productOrder.getTotalPrice();
                Price offeredPrice = productOrder.getOfferedPrice();
                if (offeredPrice != null) {
                    Price convertedOfferedPrice = productOrder.getConvertedOfferedPrice();
                    boolean currencyConversion = convertedOfferedPrice != null && !convertedOfferedPrice.getCurrency().contentEquals(offeredPrice.getCurrency());
                    estimatedPrice.setText("Price " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(offeredPrice) +
                            (currencyConversion ? (" (" + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(convertedOfferedPrice) + ")") : "") +
                            ((offeredPrice.getValue() == totalPrice.getValue()) ? "" : (" - was " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(totalPrice))
                                    //+ (com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo().isChargeVAT() ? " (incl 20% VAT)" : "")
                            )
                    );
                    estimatedPrice.setVisible(true);
                } else if(totalPrice != null){
                    estimatedPrice.setText("Price " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(totalPrice) + " (to be confirmed)");
                    estimatedPrice.setVisible(true);
                } else {
                    // do nothing
                }
            } break;
        }
        // set estimated delivery date
        switch (status) {
            case Accepted:
            case InProduction:
            case Downloading: {
                if(productOrder.getEstimatedDeliveryDate() != null) {
                    estimatedDeliveryDate.setVisible(true);
                    estimatedDeliveryDate.setText("Expected before " + DateUtil.displaySimpleDateNoSeconds(productOrder.getEstimatedDeliveryDate()));
                }
            }
            break;
/*
            case Delivered:
            case Completed: {
                if(productOrder.getDeliveredDate() != null) {
                    estimatedDeliveryDate.setVisible(true);
                    estimatedDeliveryDate.setText("Delivered on " + DateUtil.displaySimpleUTCDate(productOrder.getDeliveredDate()));
                }
            } break;
*/
        }
        switch (status) {
            // product has been downloaded and is available locally
            // it might be processing for display
            case Completed: {
                downloadIcon.setVisible(true);
                expandProductProperties.setText("View metadata and access layer display control");
                productProperties.clear();
                productProperties.resizeRows(0);
                // add vertical align to columns style
                HTMLTable.CellFormatter formatter = productProperties.getCellFormatter();
                addTableProperty(productProperties, "File name:", productOrder.getProductFileName());
                addTableProperty(productProperties, "File size:", com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayFileSize(productOrder.getProductFileSizeBytes()));
                ExpandWidget sectionWidget = new ExpandWidget();
                sectionWidget.setExpanded(false);
                sectionWidget.setText("View product file structure");
                HTMLPanel fileList = new HTMLPanel("");
                sectionWidget.add(fileList);
                sectionWidget.setExpandHandler(event -> {
                    if(fileList.getWidgetCount() == 0) {
                        fileList.add(new LoadingIcon("Loading files from product archive..."));
                        CustomerService.App.getInstance().loadFileList(productOrder.getId(), new AsyncCallback<List<FileDTO>>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Window.alert("Could not list files in product, reason is " + caught.getMessage());
                                fileList.clear();
                                sectionWidget.setExpanded(false);
                            }

                            @Override
                            public void onSuccess(List<FileDTO> result) {
                                fileList.clear();
                                FileTreeWidget fileTreeWidget = new FileTreeWidget();
                                fileTreeWidget.getElement().getStyle().setFontSize(0.8, com.google.gwt.dom.client.Style.Unit.EM);
                                fileList.add(fileTreeWidget);
                                fileTreeWidget.setFiles(result);
                                fileTreeWidget.setPresenter(new FileTreeWidget.Presenter() {
                                    @Override
                                    public void onFileSelected(FileDTO fileDTO) {
                                        Window.open("./api/download-product/download/" + productOrder.getId() + "/" + fileDTO.getPath() + "/" + fileDTO.getName(), "_blank", null);
                                    }
                                });
                            }
                        });
                    }
                });
                addTablePropertyWidget(productProperties, "File structure", sectionWidget);
                formatter.setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);
                // add product manual download widget
                String productManualUrl = productOrder.getDownloadManualURL();
                if(!StringUtils.isEmpty(productManualUrl)) {
                    Anchor anchor = new Anchor("download manual");
                    anchor.addStyleName(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.style().eiBlueAnchor());
                    anchor.setHref(productManualUrl);
                    anchor.setTarget("_blank");
                    addTablePropertyWidget(productProperties, "Product manual", anchor);
                }
                addTableProperty(productProperties, "Delivered on:", DateUtil.displaySimpleUTCDate(productOrder.getDeliveredDate()));
                addTableProperty(productProperties, "Area covered:", Utils.formatSurface(Utils.FORMAT.SQKILOMETERS, 1, MapPanel.getPathArea(productOrder.getCoordinates())));
                if(productOrder.getPaidPrice() != null) {
                    addTableProperty(productProperties, "Price paid:", com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(productOrder.getPaidPrice()));
                }
                PUBLICATION_STATUS publicationStatus = productOrder.getPublicationStatus();
                if(publicationStatus != null) {
                    switch (publicationStatus) {
                        case Failed:
                            processing.setVisible(true);
                            processing.setResource(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.error());
                            processing.setText("Failed to publish");
                            break;
                        case Requested:
                            processing.setVisible(true);
                            processing.setResource(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.loading());
                            processing.setText("Preparing to publish...");
                            break;
                        case Publishing:
                            processing.setVisible(true);
                            processing.setResource(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.loading());
                            processing.setText("Publishing...");
                            break;
                        case Published:
                            processing.setVisible(true);
                            ArrayList<ProductMetadataDTO> publishedProducts = productOrder.getPublishedProducts();
                            if (publishedProducts != null && publishedProducts.size() > 0) {
                                processing.setResource(StyleResources.INSTANCE.settings());
                                String processingName = publishedProducts.get(0).getName();
                                processing.setText(processingName);
                                publishDownloadIcon.setVisible(true);
                                publishDownloadIcon.setHref(CustomHistorian.getHostPageBaseURL() + "api/download-product/download-processed/" + productOrder.getPublishedProducts().get(0).getPublishRequestId());
                                publishDownloadIcon.setText("Download " + processingName);
                                dashboardSelection.setVisible(true);
                                updateWorkspaces();
                                additionalProductPropertiesControls.clear();
                                SelectionWidget selectionWidget = new SelectionWidget();
                                selectionWidget.setText("Activate clipping");
                                selectionWidget.setSelectHandler(new SelectedBox.SelectHandler() {
                                    @Override
                                    public void onSelected(boolean selected) {
                                        productChangeHandlers.activateClipping(selected);
                                    }
                                });
                                additionalProductPropertiesControls.add(selectionWidget);
                                // update thumbnail
                                thumbnailImage.setUrl(productOrder.getThumbnailURL());
                            } else {
                                processing.setResource(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.loading());
                                processing.setText("Finalising publishing...");
                            }
                            break;
                    }
                }
            }
            break;
        }
    }

    private void addFormatOption(MenuArrowedPanel downloadSelectionLabel, String format) {
        Label label = new Label(format);
        downloadSelectionLabel.add(label);
        label.addClickHandler(event -> {productChangeHandlers.requestWCS(format);});
    }

    private void displayProductProperties(ProductOrderDTO productOrderDTO) {
        productProperties.clear();
        productProperties.resizeRows(0);
    }

    /*
	 * get the product request to be added to the user order
	 * throws an exception if a parameter value is not valid
	 */
	public ProductOrderDTO getProductOrder() {
		return productOrder;
	}

    public void setProductChangeHandlers(ProductChangeHandlers productChangeHandlers) {
        this.productChangeHandlers = productChangeHandlers;
        setBaseProductChangeHandlers(productChangeHandlers);
    }

    public void setDraggableInto() {

        String targetProductId = productOrder.getId();
        this.addBitlessDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                event.preventDefault();
                // for security reasons drag over doesn't allow access to the data
/*
                String targetProductId = event.getData("text");
                printLog("On drag over for target product id " + targetProductId + " and productId " + productId);
                if(targetProductId != null && targetProductId.length() > 0 && !targetProductId.contentEquals(productId)) {
                    addStyleName(style.dragOver());
                }
*/
                addStyleName(style.dragOver());
            }
        }, DragOverEvent.getType());

        this.addBitlessDomHandler(new DragLeaveHandler() {
            @Override
            public void onDragLeave(DragLeaveEvent event) {
                setStyleName(style.dragOver(), false);
            }
        }, DragLeaveEvent.getType());

        this.addBitlessDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                event.preventDefault();
                String productId = event.getData("text");
                if(targetProductId != null && targetProductId.length() > 0 && !targetProductId.contentEquals(productId)) {
                    setStyleName(style.dragOver(), false);
                    EINEO.clientFactory.getEventBus().fireEvent(new ProductOrderPlaceChange(targetProductId, productId));
                }
            }
        }, DropEvent.getType());

    }

    public void updateThumbnail() {
        setThumbnail(productOrder.getThumbnailURL());
    }

    public void updateWorkspaces() {
        List<WorkspaceSummaryDTO> workspaces = productOrder.getWorkspaces();
        dashboardsSelectionWidget.setText(workspaces != null && workspaces.size() > 0 ?
                (workspaces.size() + " - " + ListUtil.toString(workspaces, value -> {return value.getName();}, ",")) :
                "no workspace selected");
    }

}
