package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.place.AISettingsPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.admin.client.view.AISettingsView;
import com.geocento.webapps.earthimages.emis.common.share.entities.Settings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.HashMap;

public class AISettingsActivity extends AIApplicationTemplateActivity implements AISettingsView.Presenter {

	private AISettingsView aiSettingsView;

	private AISettingsPlace place;

    public Settings settings;

    public AISettingsActivity(AISettingsPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		aiSettingsView = clientFactory.getAISettingsView();
        initialiseTemplate(aiSettingsView.getTemplateView());
		panel.setWidget(aiSettingsView.asWidget());
        aiSettingsView.setPresenter(this);
        displaySave(false);
		bind();
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        // nothing to do

        // load the sensors
        loadSettings();

        // load Planet account values
        loadAccountValues();
	}

    private void loadSettings() {
        aiSettingsView.displayLoading("Loading settings...");
        AdministrationService.App.getInstance().loadSettingValues(new AsyncCallback<Settings>() {

            @Override
            public void onFailure(Throwable caught) {
                aiSettingsView.hideLoading();
                aiSettingsView.displayError("Could not load sensors from server");
            }

            @Override
            public void onSuccess(Settings result) {
                settings = result;
                aiSettingsView.hideLoading();
                aiSettingsView.displaySettings(settings);
            }
        });
    }

    private void loadAccountValues() {
        AdministrationService.App.getInstance().loadAccountValues(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                aiSettingsView.hideLoading();
                aiSettingsView.displayError("Could not load Planet account values from server");
            }

            @Override
            public void onSuccess(String result) {
                aiSettingsView.displayAccountValues(result);
            }
        });
    }

    @Override
	protected void bind() {

        handlers.add(aiSettingsView.getSaveButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                aiSettingsView.displayLoading("Updating settings...");
                AdministrationService.App.getInstance().updateSettings(settings, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        aiSettingsView.hideLoading();
                        aiSettingsView.displayError("Could not save settings reason is " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        aiSettingsView.hideLoading();
                        aiSettingsView.displaySuccess("Settings have been updated");
                        displaySave(false);
                    }
                });
            }
        }));

 	}

    @Override
    public void settingsHaveChanged() {
        displaySave(true);
    }

    private void displaySave(boolean display) {
        aiSettingsView.displaySave(display);
    }

}
