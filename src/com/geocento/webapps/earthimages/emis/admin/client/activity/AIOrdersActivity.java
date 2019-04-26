package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.*;
import com.geocento.webapps.earthimages.emis.admin.client.place.AIOrdersPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.admin.client.view.AIOrdersView;
import com.geocento.webapps.earthimages.emis.admin.client.widgets.UploadProductFormPopup;
import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.common.client.popup.LoadingPanel;
import com.geocento.webapps.earthimages.emis.common.client.popup.PopupPropertyEditor;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.property.domain.ChoiceProperty;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.property.domain.TextProperty;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AIOrdersActivity extends AIApplicationTemplateActivity implements AIOrdersView.Presenter {

	private AIOrdersView aiOrdersView;

	private AIOrdersPlace place;

    private ProductOrderDTO productOrderDTO;

    private int start;
    private int limit;
    private String sortBy;
    private boolean isAscending;

    public AIOrdersActivity(AIOrdersPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		aiOrdersView = clientFactory.getAIOrdersView();
        initialiseTemplate(aiOrdersView.getTemplateView());
		panel.setWidget(aiOrdersView.asWidget());
        aiOrdersView.setPresenter(this);
		bind();
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        // nothing to do

        // load the sensors
        rangeChanged(0, 10, "createdOn", false);
	}

    private void loadOrders() {
        aiOrdersView.displayLoading("Loading product orders...");
        AdministrationService.App.getInstance().loadProductOrders(start, limit, sortBy, isAscending, new AsyncCallback<List<ProductOrderDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                aiOrdersView.hideLoading();
                aiOrdersView.displayError("Could not load product orders from server");
            }

            @Override
            public void onSuccess(List<ProductOrderDTO> productOrderDTOs) {
                aiOrdersView.hideLoading();
                aiOrdersView.setOrders(start, limit, sortBy, isAscending, productOrderDTOs);
            }
        });
    }

    @Override
	protected void bind() {

        handlers.add(aiOrdersView.getRefreshButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                loadOrders();
            }
        }));

        activityEventBus.addHandler(EditProductOrder.TYPE, new EditProductOrderHandler() {

            @Override
            public void onEditProductOrder(EditProductOrder event) {
                productOrderDTO = event.getProductOrderDTO();
                aiOrdersView.editProductOrder(productOrderDTO);
            }
        });

        activityEventBus.addHandler(ProductOrderChanged.TYPE, new ProductOrderChangedHandler() {
            @Override
            public void onProductOrderChanged(ProductOrderChanged event) {
                AdministrationService.App.getInstance().updateProductOrder(event.getProductOrderDTO().getId(), event.getProductOrderDTO().getStatus(), event.getProductOrderDTO().getEstimatedDeliveryTime(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Could not update product order, reason is " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        loadOrders();
                    }
                });
            }
        });

        activityEventBus.addHandler(UploadProduct.TYPE, new UploadProductHandler() {
            @Override
            public void onUploadProduct(UploadProduct event) {
                productOrderDTO = event.getProductOrder();
                // display download popup
                PopupPropertyEditor popup = PopupPropertyEditor.getInstance();
                popup.edit("Stage product", null,
                        ListUtil.toList(new Property[] {
                                new ChoiceProperty("Type of staging", "Select the type of download for this product", null, true, true, new String[] {"HTTP", "FTP", "Upload"})
                        }), new CompletionHandler<List<Property>>() {
                            @Override
                            public void onCompleted(List<Property> result) throws ValidationException {
                                popup.hide();
                                List<Property> properties = new ArrayList<Property>();
                                int index = 0;
                                String downloadType = (String) result.get(index++).getValue();
                                if(downloadType.contentEquals("Upload")) {
                                    UploadProductFormPopup.getInstance().showUpload(productOrderDTO.getId(), new UploadProductFormPopup.Presenter() {
                                        @Override
                                        public void handleUploadingCancelled() {

                                        }

                                        @Override
                                        public void handleImageUploadedError(String string) {

                                        }

                                        @Override
                                        public void handleUploadingStarted() {
                                            aiOrdersView.displayLoading("Uploading product...");
                                        }

                                        @Override
                                        public void handleProductUploaded(String imageUrl) {
                                            loadOrders();
                                        }
                                    });
                                } else {
                                    switch (downloadType) {
                                        case "HTTP": {
                                            properties.add(new TextProperty("URL", null, null, true));
                                            properties.add(new TextProperty("User name", null, null, true));
                                            properties.add(new TextProperty("User password", null, null, true));
                                        }
                                        break;
                                        case "FTP": {
                                            properties.add(new TextProperty("Server", null, null, true));
                                            properties.add(new TextProperty("Directory", null, null, true));
                                            properties.add(new TextProperty("User name", null, null, true));
                                            properties.add(new TextProperty("User password", null, null, true));
                                            properties.add(new TextProperty("File name", null, null, true));
                                        }
                                        break;
                                    }
                                    popup.edit("Download parameters", "Specify the download parameters for this product download",
                                            properties, new CompletionHandler<List<Property>>() {
                                                @Override
                                                public void onCompleted(List<Property> result) throws ValidationException {
                                                    LoadingPanel.getInstance().show("Submitting product download...");
                                                    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                                                        @Override
                                                        public void onFailure(Throwable caught) {
                                                            LoadingPanel.getInstance().hide();
                                                            Window.alert("Failed to submit product download, reason is " + caught.getMessage());
                                                        }

                                                        @Override
                                                        public void onSuccess(Void result) {
                                                            LoadingPanel.getInstance().hide();
                                                            loadOrders();
                                                        }
                                                    };
                                                    switch (downloadType) {
                                                        case "HTTP": {
                                                            int index = 0;
                                                            String url = (String) result.get(index++).getValue();
                                                            String userName = (String) result.get(index++).getValue();
                                                            String userPassword = (String) result.get(index++).getValue();
                                                            AdministrationService.App.getInstance().downloadProductHttp(productOrderDTO.getId(),
                                                                    url, userName, userPassword, callback);
                                                        }
                                                        break;
                                                        case "FTP": {
                                                            int index = 0;
                                                            String host = (String) result.get(index++).getValue();
                                                            String directory = (String) result.get(index++).getValue();
                                                            String userName = (String) result.get(index++).getValue();
                                                            String userPassword = (String) result.get(index++).getValue();
                                                            String fileName = (String) result.get(index++).getValue();
                                                            AdministrationService.App.getInstance().downloadProductFTP(productOrderDTO.getId(),
                                                                    host, directory, userName, userPassword, fileName, callback);
                                                        }
                                                        break;
                                                    }
                                                }

                                                @Override
                                                public void onCancel() {

                                                }
                                            });
                                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                                        @Override
                                        public void execute() {
                                            popup.show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancel() {

                            }
                        });

            }
        });

        activityEventBus.addHandler(MakeSample.TYPE, new MakeSampleHandler() {

            @Override
            public void onMakeSample(MakeSample event) {
                productOrderDTO = event.getProductOrderDTO();
                if(productOrderDTO.getStatus() != PRODUCTORDER_STATUS.Completed) {
                    Window.alert("Product order is not completed and cannot be used for sample");
                    return;
                }
                aiOrdersView.editSample(productOrderDTO);
            }
        });

        activityEventBus.addHandler(ChangeOrder.TYPE, event -> {
            productOrderDTO = event.getProductOrder();
            aiOrdersView.changeProductOrder(productOrderDTO);
        });
    }

    @Override
    public void filterHasChanged() {
    }

    @Override
    public void rangeChanged(int start, int limit, String sortBy, boolean isAscending) {
        this.start = start;
        this.limit = limit;
        this.sortBy = sortBy;
        this.isAscending = isAscending;
        loadOrders();
    }

    @Override
    public void createNewProduct(String orderId, String title, String description, String aoiWKT, String selectionWKT, String productId) {
        AdministrationService.App.getInstance().createProductOrder(orderId, title, description, aoiWKT, selectionWKT, productId, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not update product order, reason is " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                loadOrders();
            }
        });
    }

    @Override
    public void createNewOrder(String title, String description, String userName) {
        aiOrdersView.displayLoading("Creating order...");
        AdministrationService.App.getInstance().createNewOrder(title, description, userName, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                aiOrdersView.hideLoading();
                Window.alert("Could not create new order, reason is " + caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                aiOrdersView.hideLoading();
                aiOrdersView.displayAddProduct(result);
            }
        });
    }

    @Override
    public void createSample(String productOrderId, String title, String description, String keyWords) {
        aiOrdersView.displayLoading("Creating sample...");
        AdministrationService.App.getInstance().createSample(productOrderId, title, description, keyWords, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                aiOrdersView.hideLoading();
                Window.alert("Could not create new sample, reason is " + caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                aiOrdersView.hideLoading();
                Window.alert("Sample created successfully");
            }
        });
    }

    @Override
    public void changeOrder(String productOrderId, String orderId, Boolean copyProduct) {
        aiOrdersView.displayLoading("Changing product order...");
        AdministrationService.App.getInstance().changeOrder(productOrderId, orderId, copyProduct, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not change product order, reason is " + caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {
                aiOrdersView.hideLoading();
                Window.alert("Order changed successfully");
            }
        });
    }

}
