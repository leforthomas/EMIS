package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.IsWidget;

public interface AISignInView extends IsWidget {

    interface Presenter {

		String checkUserName(String username);
		
	}

	void setPresenter(Presenter presenter);

	HasText getUserName();

	HasText getPassword();

    HasClickHandlers getSignInButton();

	HasKeyPressHandlers getPasswordBox();

	void displayLoading(String message);

	void displayErrorMessage(String message);

	void hideMessage();

	void resetPanels();

	void lockLinks(boolean locked);

	boolean withSaveSession();

}
