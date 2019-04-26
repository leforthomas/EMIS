package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.share.UserDTO;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

public interface AIUsersView extends IsWidget {

    AIApplicationTemplateView getTemplateView();

    void displayError(String message);

    void displayLoading(String message);

    void hideLoading();

    void displayUsers(int start, int limit, String sortBy, boolean isAscending, String keyWords, List<UserDTO> users);

    void refreshUsers();

    HasClickHandlers getCreateUser();

    HasClickHandlers getRefresh();

    void displayCreateUser();

    String getKeywords();

    int getStart();

    int getLimit();

    String getSortBy();

    boolean isAscending();

    static public interface Presenter {

    }

	void setPresenter(Presenter presenter);

}
