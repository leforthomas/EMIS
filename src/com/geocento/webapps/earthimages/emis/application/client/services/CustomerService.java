package com.geocento.webapps.earthimages.emis.application.client.services;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.LayerResource;
import com.geocento.webapps.earthimages.emis.common.share.UserLayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.metaaps.webapps.earthimages.extapi.server.domain.*;
import com.metaaps.webapps.libraries.client.property.domain.Property;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RemoteServiceRelativePath("customerservice")
public interface CustomerService extends RemoteService {

    ApplicationSettingsDTO loadApplicationSettings() throws EIException;

    List<UserLayerDTO> loadUserLayers() throws EIException;

    void deleteUserLayer(Long id) throws EIException;

    List<UserLayerDTO> addUserLayers(List<LayerDTO> selectedLayers) throws EIException;

    PaymentTransactionDTO addFundsWithCreditCard(Price amount, CreditCardRequest creditCardRequest) throws EIException;

    EventDTO loadEvent(String eventId, String password) throws EIException;

    void archiveEvent(String id) throws EIException;

    void deleteProduct(String id) throws EIException;

    void republishProductOrder(String id) throws EIException;

    String uploadOrder(String id) throws EIException;

    String addProductsToEvent(String orderId, List<ProductRequestDTO> products) throws EIException;

    PaymentTransactionDTO acceptAndMakeProductOrdersPayment(String id, HashMap<String, Price> productsPayment) throws EIException;

    List<EventSummaryDTO> loadEvents(String name, Date start, Date stop, String status) throws EIException;

    UserInformationDTO loadUserInformation() throws EIException;

    List<CreditCardToken> getUserPaymentInformation() throws EIException;

    List<PaymentTransactionDTO> getTransactionsHistory(int transactionsHistoryStart, int transactionsHistoryPageSize) throws EIException;

    List<DocumentDTO> getDocumentation() throws EIException;

    void updateUserProfile(UserInformationDTO userProfile, String password) throws EIException;

    List<FileDTO> loadFileList(String id) throws EIException;

    List<LayerResource> loadAvailableLayers(String resourceId) throws EIException;

    /**
     * Utility/Convenience class.
     * Use CustomerService.App.getInstance() to access static instance of EIExpressServiceAsync
     */
    public static class App {

        private static CustomerServiceAsync ourInstance = GWT.create(CustomerService.class);

        public static synchronized CustomerServiceAsync getInstance() {
            return ourInstance;
        }
    }

}
