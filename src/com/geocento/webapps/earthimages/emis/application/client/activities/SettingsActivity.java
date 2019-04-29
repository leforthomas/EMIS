package com.geocento.webapps.earthimages.emis.application.client.activities;

import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.place.SettingsPlace;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.utils.HubspotChatHelper;
import com.geocento.webapps.earthimages.emis.application.client.views.SettingsView;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.HashMap;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class SettingsActivity extends EMISTemplateActivity implements SettingsView.Presenter {

    private final SettingsPlace place;

    private SettingsView settingsView;

    public SettingsActivity(SettingsPlace place, ClientFactory clientFactory) {
        super(clientFactory);
        this.place = place;
        // show chat widget for settings
        HubspotChatHelper.displayChat(true);
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        super.start(panel, eventBus);
        settingsView = clientFactory.getSettingsView();
        settingsView.setPresenter(this);
        panel.setWidget(settingsView.asWidget());
        Window.setTitle("EINEO - view account and settings");
        initialiseTemplate(settingsView.getTemplateView());
        bind();
        handleHistory();
    }

    private void handleHistory() {
        // get the parameters
        HashMap<String, String> tokens = Utils.extractTokens(place.getToken());

        double prepaidValue = com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo().getPrepaidValue();
        String currency = com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo().getPrepaidCurrency();
        settingsView.displayCreditAmount(prepaidValue, currency);

        // get tab
        String tab = tokens.get(SettingsPlace.TOKENS.tab.toString());
        if(tab == null) {
            tab = SettingsPlace.TOKENS.information.toString();
        }
        SettingsPlace.TOKENS tabToken = null;
        try {
            tabToken = SettingsPlace.TOKENS.valueOf(tab);
        } catch (Exception e) {
            tabToken = SettingsPlace.TOKENS.information;
        }
        settingsView.displayTab(tabToken);
        switch (tabToken) {
            case information:
                loadUserInformation();
                break;
            case password:
                // nothing to load
                break;
            case means:
                loadPaymentMeans();
                break;
            case transactions:
                loadTransactionsHistory();
                break;
            case documentation:
                loadDocumentation();
                break;
        }

    }

    @Override
    protected void bind() {

        activityEventBus.addHandler(CreditUpdatedEvent.TYPE, event -> {
            settingsView.displayCreditAmount(event.getCreditUpdatedNotification().amount, event.getCreditUpdatedNotification().currency);
            loadTransactionsHistory();
        });
    }

    private void loadUserInformation() {

        settingsView.displayUserInformationLoading("Loading personal information...");

        CustomerService.App.getInstance().loadUserInformation(new AsyncCallback<UserInformationDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                settingsView.hideUserInformationLoading();
                settingsView.displayError("Unable to load user information, please reload application");
            }

            @Override
            public void onSuccess(UserInformationDTO result) {
                settingsView.hideUserInformationLoading();
                settingsView.displayUserInformation(result);
            }
        });
    }

    @Override
    public void loadPaymentMeans() {
        settingsView.setCreditCardsLoading("Loading credit cards...");

        CustomerService.App.getInstance().getUserPaymentInformation(new AsyncCallback<List<CreditCardToken>>() {
            @Override
            public void onFailure(Throwable throwable) {
                settingsView.hidePaymentMeansLoading();
                settingsView.setCreditCardsError(throwable.getMessage());
            }

            @Override
            public void onSuccess(List<CreditCardToken> creditCardTokens) {
                settingsView.hidePaymentMeansLoading();
                settingsView.setCreditCards(creditCardTokens);
            }
        });
    }

    @Override
    public void loadTransactionsHistory() {
        settingsView.displayTransactionsHistoryLoading("Loading transactions...");
        CustomerService.App.getInstance().getTransactionsHistory(settingsView.getTransactionsHistoryStart(), settingsView.getTransactionsHistoryPageSize(), new AsyncCallback<List<PaymentTransactionDTO>>() {

            @Override
            public void onFailure(Throwable throwable) {
                settingsView.hideTransactionsHistoryLoading();
                settingsView.setTransactionsHistoryError(throwable.getMessage());
            }

            @Override
            public void onSuccess(List<PaymentTransactionDTO> transactionTokens) {
                settingsView.setTransactionsHistory(transactionTokens);
            }
        });
    }

    @Override
    public void loadDocumentation() {
        settingsView.displayDocumentationLoading("Loading documentation...");
        CustomerService.App.getInstance().getDocumentation(new AsyncCallback<List<DocumentDTO>>() {

            @Override
            public void onFailure(Throwable throwable) {
                settingsView.hideDocumentationLoading();
                settingsView.setDocumentationError(throwable.getMessage());
            }

            @Override
            public void onSuccess(List<DocumentDTO> documentDTOs) {
                settingsView.hideDocumentationLoading();
                settingsView.setDocumentation(documentDTOs);
            }
        });
    }

    @Override
    public void updateProfile(UserInformationDTO userProfile, String password) {
        settingsView.displayLoading("Updating profile...");
        CustomerService.App.getInstance().updateUserProfile(userProfile, password, new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                settingsView.hideLoading();
                Window.alert("Could not update your profile, please try again");
            }

            @Override
            public void onSuccess(Void result) {
                settingsView.hideLoading();
                Window.alert("User profile updated successfully");
            }
        });
    }

    @Override
    public void changeRange(Integer start) {
        // TODO - add a handler for navigation?
    }
}
