package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.*;
import com.geocento.webapps.earthimages.emis.admin.client.place.AIUsersPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.admin.client.view.AIUsersView;
import com.geocento.webapps.earthimages.emis.admin.share.UserDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.TRANSACTION_TYPE;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.HashMap;
import java.util.List;

public class AIUsersActivity extends AIApplicationTemplateActivity implements AIUsersView.Presenter {

	private AIUsersView aiUsersView;

	private AIUsersPlace place;

    public AIUsersActivity(AIUsersPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
    }
    
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
		aiUsersView = clientFactory.getAIUsersView();
        initialiseTemplate(aiUsersView.getTemplateView());
		panel.setWidget(aiUsersView.asWidget());
		bind();
		handleHistory();
	}

	private void handleHistory() {
		HashMap<String, String> tokens = Utils.extractTokens(place.getToken());
        // nothing to do

        // load the users
        loadUsers(0, 15, "username", true, "");
	}

    private void loadUsers(final int start, final int limit, final String sortBy, final boolean isAscending, final String keyWords) {
        aiUsersView.displayLoading("Loading users...");
        AdministrationService.App.getInstance().loadUsers(start, limit, sortBy, isAscending, keyWords, new AsyncCallback<List<UserDTO>>() {

            @Override
            public void onFailure(Throwable caught) {
                aiUsersView.hideLoading();
                aiUsersView.displayError("Could not load sensors from server");
            }

            @Override
            public void onSuccess(List<UserDTO> result) {
                aiUsersView.hideLoading();
                aiUsersView.displayUsers(start, limit, sortBy, isAscending, keyWords, result);
            }
        });
    }

    @Override
	protected void bind() {

        activityEventBus.addHandler(LoadUsersEvent.TYPE, new LoadUsersEventHandler() {
            @Override
            public void onLoadUsers(LoadUsersEvent event) {
                loadUsers(event.getStart(), event.getLength(), event.getSortBy(), event.isAscending(), aiUsersView.getKeywords());
            }
        });

        activityEventBus.addHandler(UserChanged.TYPE, new UserChangedHandler() {
            @Override
            public void onUserChanged(UserChanged event) {
                aiUsersView.displayLoading("Updating user values...");
                AdministrationService.App.getInstance().updateUser(event.getUserDTO(), new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        aiUsersView.hideLoading();
                        aiUsersView.displayError("Could not update user on server");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        aiUsersView.hideLoading();
                        aiUsersView.refreshUsers();
                    }
                });
            }
        });

        activityEventBus.addHandler(UserCreated.TYPE, new UserCreatedHandler() {
            @Override
            public void onUserCreated(UserCreated event) {
                aiUsersView.displayLoading("Creating user...");
                AdministrationService.App.getInstance().createUser(event.getUsername(), event.getEmail(), event.getPassword(),
                        event.getFirstName(), event.getLastName(), event.getOrganisation(), event.getCountryCode(),
                        event.getDomain(), event.getUsage(),
                        event.getUserRole(), event.getUserStatus(),
                        event.getCurrency(),
                        new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                aiUsersView.hideLoading();
                                aiUsersView.displayError("Could not create user on server, reason is " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                aiUsersView.hideLoading();
                                reloadUsers();
                            }
                    });
                }
        });

        activityEventBus.addHandler(RemoveUserEvent.TYPE, new RemoveUserEventHandler() {
            @Override
            public void onRemoveUser(RemoveUserEvent event) {
                if(Window.confirm("Are you sure you want to remove user '" + event.getUserName() + "'?")) {
                    aiUsersView.displayLoading("Removing user...");
                    AdministrationService.App.getInstance().removeUser(event.getUserName(), new AsyncCallback<Void>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            aiUsersView.hideLoading();
                            aiUsersView.displayError("Failed to remove user reason is " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void result) {
                            aiUsersView.hideLoading();
                            // TODO - refresh table
                            reloadUsers();
                        }
                    });
                }
            }
        });

        activityEventBus.addHandler(AddCreditEvent.TYPE, event -> {
            TRANSACTION_TYPE transactionType = event.getTransactionType();
            double amount = event.getAmount();
            String currency = event.getCurrency();
            String comment = event.getComment();

            if(Window.confirm("Are you sure you want to add " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayRoundedPrice(new Price(amount, currency)) +
                    " to user '" + event.getUsername() + "'?")) {
                aiUsersView.displayLoading("Performing transaction...");
                AdministrationService.App.getInstance().addCreditTransaction(event.getUsername(), transactionType, amount, currency, comment, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        aiUsersView.hideLoading();
                        aiUsersView.displayError("Failed to add credit to user, reason is " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        aiUsersView.hideLoading();
                        reloadUsers();
                    }
                });
            }
        });

        handlers.add(aiUsersView.getCreateUser().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                aiUsersView.displayCreateUser();
            }
        }));

        handlers.add(aiUsersView.getRefresh().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                reloadUsers();
            }
        }));

	}

    private void reloadUsers() {
        loadUsers(aiUsersView.getStart(), aiUsersView.getLimit(), aiUsersView.getSortBy(), aiUsersView.isAscending(), aiUsersView.getKeywords());
    }

}
