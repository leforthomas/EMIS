package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.common.share.entities.Settings;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;

public interface AISettingsView extends IsWidget {

    AIApplicationTemplateView getTemplateView();

    void displayError(String message);

    void displayLoading(String message);

    void hideLoading();

    void displaySettings(Settings result);

    void displaySave(boolean display);

    HasClickHandlers getSaveButton();

    void displaySuccess(String message);

    void displayAccountValues(String result);

    static public interface Presenter {

        void settingsHaveChanged();

    }

	void setPresenter(Presenter presenter);

}
