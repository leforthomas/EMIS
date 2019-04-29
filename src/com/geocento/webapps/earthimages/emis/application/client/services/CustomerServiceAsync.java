package com.geocento.webapps.earthimages.emis.application.client.services;

import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.LayerResource;
import com.geocento.webapps.earthimages.emis.common.share.UserLayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface CustomerServiceAsync {

    void loadApplicationSettings(AsyncCallback<ApplicationSettingsDTO> applicationSettingsDTOAsyncCallback);

    void loadUserLayers(AsyncCallback<List<UserLayerDTO>> listAsyncCallback);

    void deleteUserLayer(Long id, AsyncCallback<Void> voidAsyncCallback);

    void addUserLayers(List<LayerDTO> selectedLayers, AsyncCallback<List<UserLayerDTO>> listAsyncCallback);

    void loadEvent(String eventId, String password, AsyncCallback<EventDTO> asyncCallback);

    void archiveEvent(String id, AsyncCallback<Void> asyncCallback);

    void deleteProduct(String id, AsyncCallback<Void> voidAsyncCallback);

    void republishProductOrder(String id, AsyncCallback<Void> voidAsyncCallback);

    void uploadOrder(String id, AsyncCallback<String> stringAsyncCallback);

    void acceptAndMakeProductOrdersPayment(String id, HashMap<String, Price> productsPayment, AsyncCallback<PaymentTransactionDTO> paymentTransactionDTOAsyncCallback);

    void loadEvents(String name, Date start, Date stop, String status, AsyncCallback<List<EventSummaryDTO>> archived);

    void addFundsWithCreditCard(Price fundPrice, CreditCardRequest creditCardRequest, AsyncCallback<PaymentTransactionDTO> paymentTransactionDTOAsyncCallback);

    void loadUserInformation(AsyncCallback<UserInformationDTO> userInformationDTOAsyncCallback);

    void getUserPaymentInformation(AsyncCallback<List<CreditCardToken>> listAsyncCallback);

    void getTransactionsHistory(int transactionsHistoryStart, int transactionsHistoryPageSize, AsyncCallback<List<PaymentTransactionDTO>> listAsyncCallback);

    void getDocumentation(AsyncCallback<List<DocumentDTO>> listAsyncCallback);

    void updateUserProfile(UserInformationDTO userProfile, String password, AsyncCallback<Void> user_profile_updated_successfully);

    void loadFileList(String id, AsyncCallback<List<FileDTO>> blank);

    void loadAvailableLayers(String resourceId, AsyncCallback<List<LayerResource>> listAsyncCallback);

    void addProductsToEvent(String orderId, List<ProductRequestDTO> products, AsyncCallback<String> async);
}
