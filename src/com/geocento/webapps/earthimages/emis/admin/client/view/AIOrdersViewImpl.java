package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.client.Admin;
import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.ProductOrderChanged;
import com.geocento.webapps.earthimages.emis.admin.client.place.AIOrdersPlace;
import com.geocento.webapps.earthimages.emis.admin.client.widgets.OrdersList;
import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.common.client.popup.LoadingPanel;
import com.geocento.webapps.earthimages.emis.common.client.popup.PopupPropertyEditor;
import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.property.domain.*;
import com.metaaps.webapps.libraries.client.widget.AsyncPagingCellTable;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.FileUploadPopup;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.Date;
import java.util.List;

public class AIOrdersViewImpl extends Composite implements AIOrdersView {

	private static AIPublishViewImplUiBinder uiBinder = GWT
			.create(AIPublishViewImplUiBinder.class);

    interface AIPublishViewImplUiBinder extends UiBinder<Widget, AIOrdersViewImpl> {
	}

	static private StyleResources styles = GWT.create(StyleResources.class);

	public interface Style extends CssResource {
	}

	@UiField
    Style style;

	@UiField
    AIApplicationTemplateView templateView;
    @UiField
    Button refreshLogfile;
    @UiField
    TextBox filter;
    @UiField
    OrdersList ordersList;
    @UiField
    Button addNewProduct;
    @UiField
    Button createOrder;
    @UiField
    Button uploadLicense;
    @UiField
    Button downloadOrders;

    private ClientFactory clientFactory;

	private Presenter presenter;
	
	public AIOrdersViewImpl(ClientFactory clientFactory) {
		
		this.clientFactory = clientFactory;

        initWidget(uiBinder.createAndBindUi(this));

        templateView.setPlace(new AIOrdersPlace());

        ordersList.setPresenter(new AsyncPagingCellTable.Presenter() {
            @Override
            public void rangeChanged(int start, int length, Column<?, ?> column, boolean isAscending) {
                presenter.rangeChanged(start, length, ordersList.getSortBy(), isAscending);
            }
        });

        filter.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                presenter.filterHasChanged();
            }
        });

        createOrder.addClickHandler(event -> {
            List<Property> properties = ListUtil.toList(new Property[] {
                    new TextProperty("Title", null, null, true, 5, 100),
                    new TextProperty("Description", null, null, true, 5, 255),
                    new TextProperty("User name", null, null, true, 5, 100)
            });
            PopupPropertyEditor.getInstance().edit("Create new order",
                    properties,
                    new CompletionHandler<List<Property>>() {
                        @Override
                        public void onCompleted(List<Property> result) throws ValidationException {
                            int index = 0;
                            String title = (String) result.get(index++).getValue();
                            String description = (String) result.get(index++).getValue();
                            String userName = (String) result.get(index++).getValue();
                            presenter.createNewOrder(title, description, userName);
                            PopupPropertyEditor.getInstance().hide();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        });

        addNewProduct.addClickHandler(event -> {
            displayAddProduct(null);
        });

        uploadLicense.addClickHandler(event -> {
            PopupPropertyEditor.getInstance().edit("Upload signed license", null,
                    ListUtil.toList(new Property[]{
                            new TextProperty("User name", null, null, true, 1, 100),
                            new IntegerProperty("Policy id", null, null, true, 0, Integer.MAX_VALUE),
                            new TextProperty("Order id", null, null, true)
                    }), new CompletionHandler<List<Property>>() {
                        @Override
                        public void onCompleted(List<Property> result) throws ValidationException {
                            int index = 0;
                            String userName = (String) result.get(index++).getValue();
                            Integer policyId = (Integer) result.get(index++).getValue();
                            String orderId = (String) result.get(index++).getValue();
                            FileUploadPopup fileUploadPopup = new FileUploadPopup("./api/license/signed/upload/" + userName + "/" + policyId + "/" +
                                    (orderId == null || orderId.length() == 0 ? "" : ("?orderId=" + orderId)));
                            fileUploadPopup.setTitleText("Upload signed license");
                            fileUploadPopup.setPresenter(new FileUploadPopup.Presenter() {
                                @Override
                                public void fileUploaded(String result) {
                                    Window.alert("File uploaded");
                                }

                                @Override
                                public void errorUploading(String message) {
                                    Window.alert("File failed to upload reason is " + message);
                                }
                            });
                            fileUploadPopup.showAt(null, null);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        });

        downloadOrders.addClickHandler(event -> Window.open("./api/orders/products/all/download/csv", "_blank", null));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public AIApplicationTemplateView getTemplateView() {
        return templateView;
    }

    @Override
    public void displayError(String message) {
        Window.alert(message);
    }

    @Override
    public void displayLoading(String message) {
        LoadingPanel.getInstance().show(message);
    }

    @Override
    public void hideLoading() {
        LoadingPanel.getInstance().hide();
    }

    @Override
    public HasClickHandlers getRefreshButton() {
        return refreshLogfile;
    }

    @Override
    public void displaySuccess(String message) {
        Window.alert(message);
    }

    @Override
    public String getFilter() {
        return filter.getValue();
    }

    @Override
    public void editProductOrder(ProductOrderDTO productOrderDTO) {
        PopupPropertyEditor.getInstance().edit("Edit product order", null,
                ListUtil.toList(new Property[]{
                        new ChoiceProperty("Status of the product order", null,
                                productOrderDTO.getStatus().toString(), true, true, Utils.enumNameToStringArray(PRODUCTORDER_STATUS.values())),
                        new DateProperty("Expected delivery time", null, productOrderDTO.getEstimatedDeliveryTime(), true)
                }), new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        productOrderDTO.setStatus(PRODUCTORDER_STATUS.valueOf((String) result.get(index++).getValue()));
                        productOrderDTO.setEstimatedDeliveryTime((Date) result.get(index++).getValue());
                        Admin.clientFactory.getEventBus().fireEvent(new ProductOrderChanged(productOrderDTO));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    public void setOrders(int start, int limit, String sortBy, boolean isAscending, List<ProductOrderDTO> orders) {
	    ordersList.setSortBy(sortBy, isAscending);
        ordersList.setRowData(start, orders);
    }

    @Override
    public void displayAddProduct(String orderId) {
        List<Property> properties = ListUtil.toList(new Property[] {
                new TextProperty("Order ID", null, orderId, orderId == null, 5, 100),
                new TextProperty("Title", null, null, true, 0, 100),
                new TextProperty("Description", null, null, true, 0, 100),
                new TextProperty("AoI WKT", null, null, true, 0, 10000),
                new TextProperty("Selection WKT", null, null, true, 0, 10000),
                new TextProperty("Product ID", null, null, true, 5, 100)
        });
        PopupPropertyEditor.getInstance().edit("Add new product to order",
                properties,
                new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        String orderId = (String) result.get(index++).getValue();
                        String title = (String) result.get(index++).getValue();
                        String description = (String) result.get(index++).getValue();
                        String aoiWKT = (String) result.get(index++).getValue();
                        String selectionWKT = (String) result.get(index++).getValue();
                        String productId = (String) result.get(index++).getValue();
                        presenter.createNewProduct(orderId, title, description,
                                aoiWKT, selectionWKT, productId);
                        PopupPropertyEditor.getInstance().hide();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    public void editSample(ProductOrderDTO productOrderDTO) {
        List<Property> properties = ListUtil.toList(new Property[] {
                new TextProperty("Product Order ID", null, productOrderDTO.getId(), false),
                new TextProperty("Title", null, null, true, 0, 100),
                new TextProperty("Description", null, null, true, 0, 200),
                new TextProperty("Keywords", null, null, true, 0, 10000)
        });
        PopupPropertyEditor.getInstance().edit("Publish product as sample value",
                properties,
                new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        String productOrderId = (String) result.get(index++).getValue();
                        String title = (String) result.get(index++).getValue();
                        String description = (String) result.get(index++).getValue();
                        String keyWords = (String) result.get(index++).getValue();
                        presenter.createSample(productOrderId, title, description, keyWords);
                        PopupPropertyEditor.getInstance().hide();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    public void changeProductOrder(ProductOrderDTO productOrderDTO) {
        List<Property> properties = ListUtil.toList(new Property[] {
                new TextProperty("New Order ID", null, null, true),
                new BooleanProperty("Remove after transfer", null, false, true)
        });
        PopupPropertyEditor.getInstance().edit("Copy the product order to other order",
                properties,
                new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        String orderId = (String) result.get(index++).getValue();
                        Boolean removeProduct = (Boolean) result.get(index++).getValue();
                        presenter.changeOrder(productOrderDTO.getId(), orderId, !removeProduct);
                        PopupPropertyEditor.getInstance().hide();
                    }

                    @Override
                    public void onCancel() {

                    }
                });

    }

}
