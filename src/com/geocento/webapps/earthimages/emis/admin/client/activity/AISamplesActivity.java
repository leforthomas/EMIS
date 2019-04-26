package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.*;
import com.geocento.webapps.earthimages.emis.admin.client.place.AISamplesPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.admin.client.view.AISamplesView;
import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.HashMap;
import java.util.List;

public class AISamplesActivity extends AIApplicationTemplateActivity implements AISamplesView.Presenter {

	private AISamplesView aiSamplesView;

	private AISamplesPlace place;

    private SampleDTO editedSample;

    private int start;
    private int limit;
    private String sortBy;
    private boolean isAscending;

    public AISamplesActivity(AISamplesPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		aiSamplesView = clientFactory.getAISamplesView();
        initialiseTemplate(aiSamplesView.getTemplateView());
		panel.setWidget(aiSamplesView.asWidget());
        aiSamplesView.setPresenter(this);
		bind();
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        // nothing to do

        // load the samples
        rangeChanged(0, 10, "sampleName", false);
	}

    private void loadSamples() {
        aiSamplesView.displayLoading("Loading log files...");
        AdministrationService.App.getInstance().loadSamples(start, limit, sortBy, isAscending, new AsyncCallback<List<SampleDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                aiSamplesView.hideLoading();
                aiSamplesView.displayError("Could not load product orders from server");
            }

            @Override
            public void onSuccess(List<SampleDTO> sampleDTOs) {
                aiSamplesView.hideLoading();
                aiSamplesView.setSamples(start, limit, sortBy, isAscending, sampleDTOs);
            }
        });
    }

    @Override
	protected void bind() {

        handlers.add(aiSamplesView.getRefreshButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                loadSamples();
            }
        }));

        activityEventBus.addHandler(EditSample.TYPE, event -> {
            editedSample = event.getSampleDTO();
            aiSamplesView.editSample(editedSample);
        });

        activityEventBus.addHandler(SampleChanged.TYPE, event -> AdministrationService.App.getInstance().updateSample(event.getSample(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not update sample, reason is " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                loadSamples();
            }
        }));

        activityEventBus.addHandler(DeleteSample.TYPE, event -> removeSample(event.getSampleDTO()));

    }

    private void removeSample(SampleDTO sampleDTO) {
        if(!Window.confirm("Are you sure you want to remove the sample '" + sampleDTO.getName() + "'")) {
            return;
        }
        aiSamplesView.displayLoading("Removing sample...");
        AdministrationService.App.getInstance().removeSample(sampleDTO.getId(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                aiSamplesView.hideLoading();
                aiSamplesView.displayError("Could not remove sample from server");
            }

            @Override
            public void onSuccess(Void result) {
                aiSamplesView.hideLoading();
                loadSamples();
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
        loadSamples();
    }

}
