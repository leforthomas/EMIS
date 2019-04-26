package com.geocento.webapps.earthimages.emis.admin.client;


import com.geocento.webapps.earthimages.emis.admin.client.activity.AppActivityMapper;
import com.geocento.webapps.earthimages.emis.admin.client.activity.Utils;
import com.geocento.webapps.earthimages.emis.admin.client.place.AISignInPlace;
import com.geocento.webapps.earthimages.emis.admin.client.place.AppPlaceHistoryMapper;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.activity.shared.FilteredActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.property.editor.PropertyEditor;
import com.metaaps.webapps.libraries.client.widget.style.StyleResources;

/**
 * Created by thomas on 9/01/15.
 */
public class Admin implements EntryPoint {

    private SimplePanel appWidget = new SimplePanel();

    public static ClientFactory clientFactory;

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
                    return new AISignInPlace("", place);
                } else {
                    return place;
                }
            }
        }, activityMapper);
        ActivityManager activityManager = new ActivityManager(filteredActivityMapper, eventBus);
        activityManager.setDisplay(appWidget);

        // Start PlaceHistoryHandler with our PlaceHistoryMapper
        AppPlaceHistoryMapper historyMapper= GWT.create(AppPlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
        historyHandler.register(placeController, eventBus, clientFactory.getDefaultPlace());

        RootLayoutPanel.get().add(appWidget);

        initialise();

        Utils.checkSignIn(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                historyHandler.handleCurrentHistory();
            }

            @Override
            public void onSuccess(Void result) {
                historyHandler.handleCurrentHistory();
            }
        });

        // remove the loading div
        DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading"));
    }

    private void initialise() {
        // make sure library css is injected
        // make sure common and applications css is injected
        // TODO - move to library?
        StyleResources.INSTANCE.style().ensureInjected();
        com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.style().ensureInjected();
        // initialise the property editor with editors
        PropertyEditor.registerDefaultEditors();
    }

}
