package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.application.client.place.SettingsPlace;
import com.geocento.webapps.earthimages.emis.application.share.CreditCardToken;
import com.geocento.webapps.earthimages.emis.application.share.DocumentDTO;
import com.geocento.webapps.earthimages.emis.application.share.PaymentTransactionDTO;
import com.geocento.webapps.earthimages.emis.application.share.UserInformationDTO;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.List;

/**
 * Created by thomas on 12/02/2015.
 */
public interface SettingsView extends IsWidget {

    void displayDocumentationLoading(String message);

    void hideDocumentationLoading();

    void setDocumentationError(String message);

    void setDocumentation(List<DocumentDTO> documentDTOs);

    void displayUserInformationLoading(String message);

    void hideUserInformationLoading();

    void displayTab(SettingsPlace.TOKENS tabToken);

    public interface Presenter {

        void loadPaymentMeans();

        void loadTransactionsHistory();

        void loadDocumentation();

        void updateProfile(UserInformationDTO userProfile, String password);

        void changeRange(Integer start);
    }

    void setTransactionsHistoryError(String message);

    void setTransactionsHistory(List<PaymentTransactionDTO> paymentTransactionDTOs);

    EILiteTemplateView getTemplateView();

    void displayLoading(String message);

    void hideLoading();

    void displayError(String message);

    void displayUserInformation(UserInformationDTO result);

    void hidePaymentMeansLoading();

    int getTransactionsHistoryPageSize();

    void setCreditCardsLoading(String message);

    void setCreditCardsError(String message);

    void setCreditCards(List<CreditCardToken> creditCardTokens);

    void displayTransactionsHistoryLoading(String message);

    HasClickHandlers getSavePersonalChanges();

    HasClickHandlers getSavePasswordChanges();

    void displayCreditAmount(double amount, String currency);

    int getTransactionsHistoryStart();

    void hideTransactionsHistoryLoading();

    void setPresenter(Presenter presenter);

}
