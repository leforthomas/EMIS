package com.geocento.webapps.earthimages.emis.application.client.activities;

import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.event.MapLibraryChanged;
import com.geocento.webapps.earthimages.emis.application.client.event.OrderDisplaySettingsChanged;
import com.geocento.webapps.earthimages.emis.application.client.place.EventsPlace;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.client.utils.HubspotChatHelper;
import com.geocento.webapps.earthimages.emis.application.client.utils.SettingsHelper;
import com.geocento.webapps.earthimages.emis.application.client.views.EventsView;
import com.geocento.webapps.earthimages.emis.application.share.EventSummaryDTO;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class EventsActivity extends EMISTemplateActivity implements EventsView.Presenter {

    private final EventsPlace place;

    private EventsView eventsView;

    private Date startTime;
    private Date stopTime;

    public EventsActivity(EventsPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
        // show chat widget for events
        HubspotChatHelper.displayChat(true);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        eventsView = clientFactory.getOrdersView();
        eventsView.setPresenter(this);
        panel.setWidget(eventsView.asWidget());
        initialiseTemplate(eventsView.getTemplateView().getTemplateView());
        eventsView.clearAll();
        eventsView.displayOrderContent();
        Window.setTitle("Application - Manage Ordered Satellite Imagery");
        bind();
        SettingsHelper.loadCookies();
        eventsView.setMapPanelDisplaySettings(SettingsHelper.getOverlayTransparency(), SettingsHelper.getProductSelectionOpacity());
        eventsView.loadMapLibrary(SettingsHelper.getBaseMapId(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                eventsView.hideLoading();
                eventsView.displayErrorMessage(caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                eventsView.hideLoading();
                handleHistory();
            }
        });
    }

    private void handleHistory() {
        // get the parameters
        HashMap<String, String> tokens = Utils.extractTokens(place.getToken());

        // set default settings first
        eventsView.setMapBounds(
                new EOBounds(new EOLatLng(-1.0, -180.0), new EOLatLng(1.0, 180.0))
        );

        String filterText = tokens.get(EventsPlace.TOKENS.filterText.toString());
        eventsView.getNameFilter().setText(filterText == null ? "" : filterText);

        String startDateValue = tokens.get(EventsPlace.TOKENS.start.toString());
        if(startDateValue != null) {
            try {
                startTime = new Date(Long.parseLong(startDateValue));
            } catch (Exception e) {
            }
        }
        if(startTime == null) {
            startTime = DateUtil.addMonths(new Date(), -12);
        }
        String stopDateValue = tokens.get(EventsPlace.TOKENS.stop.toString());
        if(stopDateValue != null) {
            try {
                stopTime = new Date(Long.parseLong(stopDateValue));
            } catch (Exception e) {
            }
        }
        if(stopTime == null) {
            stopTime = new Date();
        }
        setTimeFrame(startTime, stopTime);
        updateFilter(filterText, startTime, stopTime, eventsView.getStatusFilter());
    }

    private void updateFilter(String name, Date start, Date stop, String status) {
        eventsView.displayOrdersLoading("Loading...");
        eventsView.clearAll();

        CustomerService.App.getInstance().loadEvents(name, start, stop, status, new AsyncCallback<List<EventSummaryDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                eventsView.displayErrorMessage("Failed to load orders, reason is: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<EventSummaryDTO> eventSummaryDTOS) {
                eventsView.displayOrderContent();
                eventsView.setOrders(eventSummaryDTOS);
                if(eventSummaryDTOS.size() > 0) {
                    EventSummaryDTO eventSummaryDTO = eventSummaryDTOS.get(0);
                    eventsView.selectOrder(eventSummaryDTO);
                    eventsView.setMapBounds(AOIUtils.getBounds(eventSummaryDTO.getAoi()));
                }
                if(status.equals("ARCHIVED"))
                    eventsView.displayOrdersMessage("Found " + eventSummaryDTOS.size() + " archived orders");
                else
                    eventsView.displayOrdersMessage("Found " + eventSummaryDTOS.size() + " active orders");
            }
        });
    }

    private void updateHistory() {
        List<String> items = new ArrayList<String>();
        String filterText = eventsView.getNameFilter().getText();
        if(!StringUtils.isEmpty(filterText)) {
            items.add(EventsPlace.TOKENS.filterText.toString());
            items.add(filterText);
        }
        if(startTime != null) {
            items.add(EventsPlace.TOKENS.start.toString());
            items.add(startTime.getTime() + "");
        }
        if(stopTime != null) {
            items.add(EventsPlace.TOKENS.stop.toString());
            items.add(stopTime.getTime() + "");
        }
        History.newItem(PlaceHistoryHelper.convertPlace(new EventsPlace(Utils.generateTokens(items.toArray(new String[items.size()])))), false);
    }

    @Override
    public void deleteEvent(String orderId) {
    }

    private void setTimeFrame(Date startTime, Date stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        // make sure dates are set to the morning and evening
        startTime.setHours(0);
        startTime.setMinutes(0);
        startTime.setSeconds(0);
        stopTime.setHours(23);
        stopTime.setMinutes(59);
        stopTime.setSeconds(59);
        eventsView.setTimeFrame(startTime, stopTime);
    }

    @Override
    public void timeFrameChanged(Date startTime, Date stopTime) {
        setTimeFrame(startTime, stopTime);
    }

    @Override
    protected void bind() {

        activityEventBus.addHandler(MapLibraryChanged.TYPE, event -> {
        });

        activityEventBus.addHandler(OrderDisplaySettingsChanged.TYPE, event -> changeDisplaySettings(event.getOverlayOpacity(), event.getProductSelectionOpacity()));

        handlers.add(eventsView.getSetFilter().addClickHandler(event -> {
            updateFilter(eventsView.getNameFilter().getText(), startTime, stopTime, eventsView.getStatusFilter());
            updateHistory();
        }));

    }

    @Override
    public void baseMapChanged(String mapId) {
        SettingsHelper.setBaseMapId(mapId);
    }

    private void changeDisplaySettings(double transparencyOverlays, double productSelectionOpacity) {
        SettingsHelper.setProductSelectionOpacity(productSelectionOpacity);
        SettingsHelper.setOverlayTransparency(transparencyOverlays);
        eventsView.setMapPanelDisplaySettings(SettingsHelper.getOverlayTransparency(), SettingsHelper.getProductSelectionOpacity());
    }
}
