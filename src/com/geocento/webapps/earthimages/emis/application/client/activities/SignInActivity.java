package com.geocento.webapps.earthimages.emis.application.client.activities;

import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.geocento.webapps.earthimages.emis.common.share.utils.UserHelper;
import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.place.SignInPlace;
import com.geocento.webapps.earthimages.emis.application.client.services.LoginService;
import com.geocento.webapps.earthimages.emis.application.client.services.LoginServiceAsync;
import com.geocento.webapps.earthimages.emis.application.client.utils.HubspotChatHelper;
import com.geocento.webapps.earthimages.emis.application.client.utils.Utils;
import com.geocento.webapps.earthimages.emis.application.client.views.SignInView;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import java.util.HashMap;

public class SignInActivity extends AbstractCustomerActivity implements SignInView.Presenter {

	private SignInView signInView;

	private SignInPlace place;
	private Place nextPlace;

	static public boolean automaticLogin = false;
    private LoginServiceAsync loginService;

    public SignInActivity(SignInPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
        // get the place to go to after login in
        nextPlace = place.getPlace();
        if(nextPlace == null || nextPlace.equals(Place.NOWHERE)) {
        	nextPlace = clientFactory.getDefaultPlace();
        }
        // do not show chat widget for signing
        HubspotChatHelper.displayChat(false);
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		signInView = clientFactory.getSignInView();
		panel.setWidget(signInView.asWidget());
	    loginService = LoginService.App.getInstance();
	    // reset all widgets before displaying
	    signInView.resetPanels();
		bind();
        signInView.setPresenter(this);
		signInView.lockLinks(false);
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = com.metaaps.webapps.libraries.client.widget.util.Utils.extractTokens(place.getToken());
        // nothing to do
	}

	@Override
	protected void bind() {
		handlers.add(
                signInView.getSignInButton().addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        signIn();
                    }
                }));

        handlers.add(
                signInView.getPasswordBox().addKeyPressHandler(new KeyPressHandler() {

                    @Override
                    public void onKeyPress(KeyPressEvent event) {
                        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                            signIn();
                        }
                    }
                }));

    }

	void signIn() {
		signInView.displayLoading("Signing you in...");
		String name = signInView.getUserName().getText();
		String password = signInView.getPassword().getText();
		if(name.length() < 5) {
			signInView.displayErrorMessage("Name should be at least 5 characters long");
			return;
		}
		if(password.length() < 5) {
			signInView.displayErrorMessage("Password should be at least 5 characters long");
			return;
		}
		signInView.lockLinks(true);
		loginService.login(name, password, new AsyncCallback<LoginInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				signInView.displayErrorMessage("Failed to communicate with Server");
				signInView.lockLinks(false);
			}

			@Override
			public void onSuccess(LoginInfo result) {
				signInView.lockLinks(false);
				if(result == null || result.isLoggedIn() == false) {
					signInView.displayErrorMessage("Unknown User/Password combination");
				} else {
                    Utils.saveSession(result, signInView.withSaveSession());
                    if(nextPlace instanceof SignInPlace) {
                        nextPlace = clientFactory.getDefaultPlace();
                    }
                    clientFactory.getEventBus().fireEvent(new PlaceChangeEvent(nextPlace));
				}
			}
			
		});
	}
	
	@Override
	public String checkUserName(String username) {
		if(username.length() < 5) {
			return "User name too short - min 5 characters";
		}
		if(!username.matches(UserHelper.usernameRegexp)) {
			return "User name can only contain alphanumeric chars";
		}
		return null;
	}

}
