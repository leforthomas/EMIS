package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.place.AISignInPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.LoginService;
import com.geocento.webapps.earthimages.emis.admin.client.services.LoginServiceAsync;
import com.geocento.webapps.earthimages.emis.admin.client.view.AISignInView;
import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.utils.UserHelper;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.HashMap;

public class AISignInActivity extends AbstractAIActivity implements AISignInView.Presenter {

	private AISignInView aiSignInView;

	private AISignInPlace place;
	private Place nextPlace;

	static public boolean automaticLogin = false;
    private LoginServiceAsync loginService;

    public AISignInActivity(AISignInPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
        // get the place to go to after login in
        nextPlace = place.getPlace();
        if(nextPlace == null || nextPlace.equals(Place.NOWHERE)) {
        	nextPlace = clientFactory.getDefaultPlace();
        }
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		aiSignInView = clientFactory.getAISignInView();
		panel.setWidget(aiSignInView.asWidget());
	    loginService = LoginService.App.getInstance();
	    // reset all widgets before displaying
	    aiSignInView.resetPanels();
		bind();
        aiSignInView.setPresenter(this);
		aiSignInView.lockLinks(false);
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        // nothing to do
	}

	@Override
	protected void bind() {
		handlers.add(
                aiSignInView.getSignInButton().addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        signIn();
                    }
                }));

        handlers.add(
                aiSignInView.getPasswordBox().addKeyPressHandler(new KeyPressHandler() {

                    @Override
                    public void onKeyPress(KeyPressEvent event) {
                        if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                            signIn();
                        }
                    }
                }));

    }

	void signIn() {
		aiSignInView.displayLoading("Signing you in...");
		String name = aiSignInView.getUserName().getText();
		String password = aiSignInView.getPassword().getText();
		if(name.length() < 5) {
			aiSignInView.displayErrorMessage("Name should be at least 5 characters long");
			return;
		}
		if(password.length() < 5) {
			aiSignInView.displayErrorMessage("Password should be at least 5 characters long");
			return;
		}
		aiSignInView.lockLinks(true);
        // check for sign up token
        HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
		loginService.login(name, password, new AsyncCallback<LoginInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				aiSignInView.displayErrorMessage("Failed to communicate with Server");
				aiSignInView.lockLinks(false);
			}

			@Override
			public void onSuccess(LoginInfo result) {
				aiSignInView.lockLinks(false);
				if(result == null || result.isLoggedIn() == false) {
					aiSignInView.displayErrorMessage("Unknown User/Password combination");
				} else {
					if(result.getUserRole() == USER_ROLE.ADMINISTRATOR) {
						com.geocento.webapps.earthimages.emis.admin.client.activity.Utils.saveSession(result, aiSignInView.withSaveSession());
						if(nextPlace instanceof AISignInPlace) {
							nextPlace = clientFactory.getDefaultPlace();
						}
						clientFactory.getEventBus().fireEvent(new PlaceChangeEvent(nextPlace));
					} else {
                        aiSignInView.displayErrorMessage("Your are not allowed to access this application.");
                    }
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
