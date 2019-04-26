package com.geocento.webapps.earthimages.emis.application.client;

import com.geocento.webapps.earthimages.emis.application.client.activities.AppActivityMapper;
import com.geocento.webapps.earthimages.emis.application.client.activities.EMISTemplateActivity;
import com.geocento.webapps.earthimages.emis.application.client.place.AppPlaceHistoryMapper;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryMapperHashBang;
import com.geocento.webapps.earthimages.emis.application.client.place.SignInPlace;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.client.utils.Utils;
import com.geocento.webapps.earthimages.emis.application.share.ApplicationSettingsDTO;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.activity.shared.FilteredActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.property.editor.PropertyEditor;

public class Application implements EntryPoint {

    private static ApplicationSettingsDTO applicationSettings;

    private static boolean firstConnection;

    public static ClientFactory clientFactory;

    private SimplePanel appWidget = new SimplePanel();

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // Create ClientFactory using deferred binding so we can replace with different
        // impls in gwt.xml
        clientFactory = GWT.create(ClientFactory.class);
        EventBus eventBus = clientFactory.getEventBus();
        final PlaceController placeController = clientFactory.getPlaceController();

        // Start ActivityManager for the main widget with our ActivityMapper
        ActivityMapper activityMapper = new AppActivityMapper(clientFactory);
        FilteredActivityMapper filteredActivityMapper = new FilteredActivityMapper(new FilteredActivityMapper.Filter() {

            @Override
            public Place filter(Place place) {
                // not logged in
                if(Utils.getLoginInfo() == null || !Utils.getLoginInfo().isLoggedIn()) {
                    return new SignInPlace("", place);
                } else {
                    return place;
                }
            }
        }, activityMapper);
        ActivityManager activityManager = new ActivityManager(filteredActivityMapper, eventBus);
        activityManager.setDisplay(appWidget);

        // Start PlaceHistoryHandler with our PlaceHistoryMapper
        AppPlaceHistoryMapper historyMapper= GWT.create(AppPlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(new PlaceHistoryMapperHashBang(historyMapper));
        historyHandler.register(placeController, eventBus, clientFactory.getDefaultPlace());

        RootLayoutPanel.get().add(appWidget);

        initialise();

        loadApplicationSettings(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not load application settings, please reload the web application");
            }

            @Override
            public void onSuccess(Void result) {
                clearLoading();
                Utils.checkSignIn(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if(EMISTemplateActivity.checkFailure(caught)) {
                            return;
                        }
                        historyHandler.handleCurrentHistory();
                    }

                    @Override
                    public void onSuccess(Void result) {
                        historyHandler.handleCurrentHistory();
                    }
                });
            }
        });

    }

    private void clearLoading() {
        // remove the loading div
        DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading"));
    }

    private void loadApplicationSettings(final AsyncCallback<Void> callback) {
        CustomerService.App.getInstance().loadApplicationSettings(new AsyncCallback<ApplicationSettingsDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                if(EMISTemplateActivity.checkFailure(caught)) {
                    return;
                }
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ApplicationSettingsDTO result) {
                applicationSettings = result;
                AOIUtils.setMaxArea(applicationSettings.getMaxArea());
                callback.onSuccess(null);
            }
        });
    }

    public static ApplicationSettingsDTO getApplicationSettings() {
        return applicationSettings;
    }

    public static boolean isFirstConnection() {
        return firstConnection;
    }

    public static void setFirstConnection(boolean firstConnection) {
        Application.firstConnection = firstConnection;
    }

    private void initialise() {
        // make sure library css is injected
        // make sure common and applications css is injected
        StyleResources.INSTANCE.style().ensureInjected();
        com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.style().ensureInjected();
        com.geocento.webapps.earthimages.emis.application.client.style.StyleResources.INSTANCE.style().ensureInjected();
        // initialise the property editor with editors
        PropertyEditor.registerDefaultEditors();
    }

    public static ClientFactory getClientFactory() {
        return clientFactory;
    }

    public static EOBounds getDefaultExtent() {
        return new EOBounds(new EOLatLng(1, 180), new EOLatLng(-1, -180));
    }
}
