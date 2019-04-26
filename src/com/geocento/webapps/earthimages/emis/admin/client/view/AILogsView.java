package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;

public interface AILogsView extends IsWidget {

    AIApplicationTemplateView getTemplateView();

    void displayError(String message);

    void displayLoading(String message);

    void hideLoading();

    void displayLogs(String logsText);

    HasClickHandlers getRefreshButton();

    void displaySuccess(String message);

    String getFilter();

    static public interface Presenter {

        void filterHasChanged();

    }

	void setPresenter(Presenter presenter);

}
