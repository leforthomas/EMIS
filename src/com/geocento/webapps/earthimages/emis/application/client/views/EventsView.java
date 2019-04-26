package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.application.share.EventSummaryDTO;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;
import com.metaaps.webapps.libraries.client.map.EOBounds;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 12/02/2015.
 */
public interface EventsView extends IsWidget {

    void setMapBounds(EOBounds bounds);

    void setBaseMapId(String baseMapId);

    void displayOrdersMessage(String message);

    void setOrders(List<EventSummaryDTO> eventSummaryDTO);

    void displayOrdersLoading(String message);

    void displayComment(String comment);

    void displayOrderContent();

    void selectOrder(EventSummaryDTO eventSummaryDTO);

    public interface Presenter {

        void baseMapChanged(String mapId);

        void deleteEvent(String orderId);

        void timeFrameChanged(Date startDate, Date stopDate);
    }

    void setTimeFrame(Date startTime, Date stopTime);

    HasClickHandlers getSetFilter();

    OrderTemplateViewImpl getTemplateView();

    void setPresenter(Presenter presenter);

    void displayErrorMessage(String message);

    void loadMapLibrary(String mapLibrary, AsyncCallback<Void> callBack);

    void setMapPanelDisplaySettings(double overlayTransparency, double productSelectionOpacity);

    void clearAll();

    void displayLoading(String message);

    void hideLoading();

    HasText getNameFilter();

    String getStatusFilter();

}
