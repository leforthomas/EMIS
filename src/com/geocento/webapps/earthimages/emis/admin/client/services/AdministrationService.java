package com.geocento.webapps.earthimages.emis.admin.client.services;

import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.geocento.webapps.earthimages.emis.admin.share.UserDTO;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.metaaps.webapps.earthimages.extapi.server.domain.Satellite;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 19/01/2015.
 */
@RemoteServiceRelativePath("adminservice")
public interface AdministrationService extends RemoteService {

    List<Satellite> loadSensors() throws EIException;

    List<UserDTO> loadUsers(int start, int limit, String sortBy, boolean isAscending, String keyWords) throws EIException;

    void updateUser(UserDTO userDTO) throws EIException;

    Settings loadSettingValues() throws EIException;

    void updateSettings(Settings settings) throws EIException;

    String loadLogFiles() throws EIException;

    void createUser(String username, String email, String password, String firstName, String lastName, String organisation, String countryCode, DOMAIN domain, USAGE usage, USER_ROLE userRole, USER_STATUS userStatus, String currency) throws EIException;

    void removeUser(String userName) throws EIException;

    void testEmail() throws EIException;

    List<ProductFetchTaskDTO> loadProductFetchTasks(int start, int limit, String sortBy, boolean isAscending) throws EIException;

    void updateProductFetchTask(ProductFetchTaskDTO productFetchTaskDTO) throws EIException;

    List<ProductOrderDTO> loadProductOrders(int start, int limit, String sortBy, boolean isAscending) throws EIException;

    void updateProductOrder(String productOrderId, PRODUCTORDER_STATUS productorderStatus, Date deliveryTime) throws EIException;

    void createProductOrder(String orderId, String title, String description, String aoiWKT, String selectionWKT, String productId) throws EIException;

    String createNewOrder(String title, String description, String userName) throws EIException;

    String createSample(String productOrderId, String title, String description, String keyWords) throws EIException;

    List<SampleDTO> loadSamples(int start, int limit, String sortBy, boolean isAscending) throws EIException;

    void updateSample(SampleDTO sample) throws EIException;

    void removeSample(String id) throws EIException;

    String changeOrder(String productOrderId, String orderId, Boolean copyProduct) throws EIException;

    void downloadProductHttp(String id, String url, String userName, String userPassword) throws EIException;

    void downloadProductFTP(String id, String host, String directory, String userName, String userPassword, String fileName) throws EIException;

    void addCreditTransaction(String username, TRANSACTION_TYPE transactionType, double amount, String currency, String comment) throws EIException;

    String loadAccountValues() throws EIException;

    /**
     * Utility/Convenience class.
     * Use AdministrationService.App.getInstance() to access static instance of AdministrationServiceAsync
     */
    public static class App {
        private static final AdministrationServiceAsync ourInstance = (AdministrationServiceAsync) GWT.create(AdministrationService.class);

        public static AdministrationServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
