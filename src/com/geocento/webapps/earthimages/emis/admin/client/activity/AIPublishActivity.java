package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.EditProductTask;
import com.geocento.webapps.earthimages.emis.admin.client.event.EditProductTaskHandler;
import com.geocento.webapps.earthimages.emis.admin.client.event.ProductTaskChanged;
import com.geocento.webapps.earthimages.emis.admin.client.event.ProductTaskChangedHandler;
import com.geocento.webapps.earthimages.emis.admin.client.place.AIPublishPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.admin.client.view.AIPublishView;
import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.HashMap;
import java.util.List;

public class AIPublishActivity extends AIApplicationTemplateActivity implements AIPublishView.Presenter {

	private AIPublishView aiPublishView;

	private AIPublishPlace place;

    private ProductFetchTaskDTO editedProductTask;

    private int start;
    private int limit;
    private String sortBy;
    private boolean isAscending;

    public AIPublishActivity(AIPublishPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		aiPublishView = clientFactory.getAIPublishView();
        initialiseTemplate(aiPublishView.getTemplateView());
		panel.setWidget(aiPublishView.asWidget());
        aiPublishView.setPresenter(this);
		bind();
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        // nothing to do

        // load the sensors
        rangeChanged(0, limit, "fetchDate", false);
	}

    private void loadPublish() {
        aiPublishView.displayLoading("Loading log files...");
        AdministrationService.App.getInstance().loadProductFetchTasks(start, limit, sortBy, isAscending, new AsyncCallback<List<ProductFetchTaskDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                aiPublishView.hideLoading();
                aiPublishView.displayError("Could not load product orders from server");
            }

            @Override
            public void onSuccess(List<ProductFetchTaskDTO> productFetchTaskDTOs) {
                aiPublishView.hideLoading();
                aiPublishView.setProductFetchTasks(start, limit, sortBy, isAscending, productFetchTaskDTOs);
            }
        });
    }

    @Override
	protected void bind() {

        handlers.add(aiPublishView.getRefreshButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                loadPublish();
            }
        }));

        activityEventBus.addHandler(EditProductTask.TYPE, new EditProductTaskHandler() {
            @Override
            public void onEditProductTask(EditProductTask event) {
                editedProductTask = event.getProductFetchTask();
                aiPublishView.editProductFetchTask(editedProductTask);
            }
        });

        activityEventBus.addHandler(ProductTaskChanged.TYPE, new ProductTaskChangedHandler() {
            @Override
            public void onProductTaskChanged(ProductTaskChanged event) {
                AdministrationService.App.getInstance().updateProductFetchTask(event.getProductFetchTaskDTO(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Could not saved task, reason is " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        loadPublish();
                    }
                });
            }
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
        loadPublish();
    }

}
