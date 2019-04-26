package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.application.share.EventDescription;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.application.share.EULARequest;
import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.application.share.WorkspaceSummaryDTO;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.metaaps.webapps.earthimages.extapi.server.domain.Comment;
import com.metaaps.webapps.libraries.client.map.EOBounds;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 12/02/2015.
 */
public interface ViewEventView extends IsWidget {

    void setMapBounds(EOBounds bounds);

    void setBaseMapId(String baseMapId);

    void addProductOrder(ProductOrderDTO productOrder);

    void displayMessage(String message);

    void setOrderStatus(ORDER_STATUS orderStatus);

    void setTimeGridTimeFrame(Date minDate, Date maxDate);

    void displayPayment(boolean display);

    void displayPaymentMessage(String message);

    void setAvailableFunds(Price price);

    void displayPayPrepaid(boolean display);

    HasClickHandlers getMakePayment();

    boolean hasAcceptedTerms();

    void displayMessageLoading(String message);

    List<ProductOrderDTO> getProductOrders();

    void displayComment(String comment);

    void displayEventContent();

    void requestPassword(AsyncCallback<String> asyncCallback);

    void displayPageLoadingError(String message);

    void setComments(List<Comment> comments);

    void updateProductOrderPublishStatuses(ProductOrderDTO productOrder);

    void scrollIntoView(ProductOrderDTO productOrder);

    void updateProductOrderPublishMap(ProductOrderDTO productOrder);

    void moveProductOrderBelow(ProductOrderDTO targetProductOrderDTO, ProductOrderDTO productOrderDTO);

    void moveProductOrder(String packageName, ProductOrderDTO productOrderDTO);

    void displayAddProductOrderWorkspace(ProductOrderDTO productOrder, List<WorkspaceSummaryDTO> workspaces);

    void updateProductOrderWorkspaces(ProductOrderDTO productOrder);

    HasClickHandlers getZoomOrder();

    HasClickHandlers getDownloadOrder();

    void updateProductOrderDisplay(ProductOrderDTO productOrderDTO);

    HasValue<Boolean> getDisplayAll();

    void setCurrentTime(Date currentTime);

    HasValue<Boolean> getDisplayAllSelections();

    void getWCSFormat(String title, String message, AsyncCallback<String> callback);

    void hideLicensesRequired();

    void showLicensesRequired(List<EULARequest> policiesToSign);

    void updateProductOrderThumbnail(ProductOrderDTO productOrder);

    void setEventDescription(EventDescription eventDescription);

    public interface Presenter {

        void baseMapChanged(String mapId);

        void archiveEvent();

        void deleteProduct(String id);

        void changePackageName(String packageName, String result);

        void handleWCSRequest(EOBounds bounds);
    }

    OrderTemplateViewImpl getTemplateView();

    void setPresenter(Presenter presenter);

    void displayWindowErrorMessage(String message);

    void loadMapLibrary(String mapLibrary, AsyncCallback<Void> callBack);

    void setMapPanelDisplaySettings(double overlayTransparency, double productSelectionOpacity);

    void deleteProductOrder(String id);

    void clearAll();

    void setAoI(AOI aoi, boolean editable);

    void displayLoadingProductOrders(String message);

    void hideLoadingProductOrders();

    void displayResultErrorMessage(String message);

    void displayLoadingPage(String message);

    void hideWindowLoading();

}
