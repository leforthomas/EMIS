package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

public interface AIOrdersView extends IsWidget {

    AIApplicationTemplateView getTemplateView();

    void displayError(String message);

    void displayLoading(String message);

    void hideLoading();

    HasClickHandlers getRefreshButton();

    void displaySuccess(String message);

    String getFilter();

    void editProductOrder(ProductOrderDTO productOrderDTO);

    void setOrders(int start, int limit, String sortBy, boolean isAscending, List<ProductOrderDTO> orders);

    void displayAddProduct(String orderId);

    void editSample(ProductOrderDTO productOrderDTO);

    void changeProductOrder(ProductOrderDTO productOrderDTO);

    static public interface Presenter {

        void filterHasChanged();

        void rangeChanged(int start, int length, String sortBy, boolean isAscending);

        void createNewProduct(String orderId, String title, String description, String aoiWKT, String selectionWKT, String productId);

        void createNewOrder(String title, String description, String userName);

        void createSample(String productOrderId, String title, String description, String keyWords);

        void changeOrder(String productOrderId, String orderId, Boolean copyProduct);
    }

	void setPresenter(Presenter presenter);

}
