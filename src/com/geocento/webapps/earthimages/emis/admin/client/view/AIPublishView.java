package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

public interface AIPublishView extends IsWidget {

    AIApplicationTemplateView getTemplateView();

    void displayError(String message);

    void displayLoading(String message);

    void hideLoading();

    HasClickHandlers getRefreshButton();

    void displaySuccess(String message);

    String getFilter();

    void editProductFetchTask(ProductFetchTaskDTO editedProductTask);

    void setProductFetchTasks(int start, int limit, String sortBy, boolean isAscending, List<ProductFetchTaskDTO> productFetchTaskDTOs);

    static public interface Presenter {

        void filterHasChanged();

        void rangeChanged(int start, int length, String sortBy, boolean isAscending);
    }

	void setPresenter(Presenter presenter);

}
