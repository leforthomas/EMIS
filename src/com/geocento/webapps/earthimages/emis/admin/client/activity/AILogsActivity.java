package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.place.AILogsPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.admin.client.view.AILogsView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.HashMap;

public class AILogsActivity extends AIApplicationTemplateActivity implements AILogsView.Presenter {

	private AILogsView aiLogsView;

	private AILogsPlace place;

    private String logFilesContent;

    public AILogsActivity(AILogsPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		aiLogsView = clientFactory.getAILogsView();
        initialiseTemplate(aiLogsView.getTemplateView());
		panel.setWidget(aiLogsView.asWidget());
        aiLogsView.setPresenter(this);
		bind();
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        // nothing to do

        // load the sensors
        loadLogs();
	}

    private void loadLogs() {
        aiLogsView.displayLoading("Loading log files...");
        AdministrationService.App.getInstance().loadLogFiles(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                aiLogsView.hideLoading();
                aiLogsView.displayError("Could not load log files from server");
            }

            @Override
            public void onSuccess(String result) {
                logFilesContent = result;
                aiLogsView.hideLoading();
                filterHasChanged();
            }
        });
    }

    @Override
	protected void bind() {

        handlers.add(aiLogsView.getRefreshButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                loadLogs();
            }
        }));

 	}

    @Override
    public void filterHasChanged() {
        filterLog(aiLogsView.getFilter());
    }

    private void filterLog(String filter) {
        if(logFilesContent == null) {
            return;
        }
        if(filter.contentEquals("")) {
            aiLogsView.displayLogs(logFilesContent);
        } else {
            String filteredText = "";
            // check each line for the filter text value
            for(String value : logFilesContent.split("\\n")) {
                if(value.contains(filter)) {
                    filteredText += value + "\\n";
                }
            }
            aiLogsView.displayLogs(filteredText);
        }
    }

}
