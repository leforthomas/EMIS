package com.geocento.webapps.earthimages.emis.admin.client.services;

import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.geocento.webapps.earthimages.emis.admin.share.UserDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.metaaps.webapps.earthimages.extapi.server.domain.Satellite;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 19/01/2015.
 */
public interface AdministrationServiceAsync {

    void loadSensors(AsyncCallback<List<Satellite>> asyncCallback);

    void loadUsers(int start, int limit, String sortBy, boolean isAscending, String keyWords, AsyncCallback<List<UserDTO>> asyncCallback);

    void updateUser(UserDTO userDTO, AsyncCallback<Void> callback);

    void loadSettingValues(AsyncCallback<Settings> asyncCallback);

    void updateSettings(Settings settings, AsyncCallback<Void> callback);

    void loadLogFiles(AsyncCallback<String> asyncCallback);

    void createUser(String username, String email, String password, String firstName, String lastName, String organisation, String countryCode, DOMAIN domain, USAGE usage, USER_ROLE userRole, USER_STATUS userStatus, String currency, AsyncCallback<Void> callback);

    void removeUser(String userName, AsyncCallback<Void> callback);

    void testEmail(AsyncCallback<Void> callback);

    void loadProductFetchTasks(int start, int limit, String sortBy, boolean isAscending, AsyncCallback<List<ProductFetchTaskDTO>> asyncCallback);

    void updateProductFetchTask(ProductFetchTaskDTO productFetchTaskDTO, AsyncCallback<Void> async);

    void loadProductOrders(int start, int limit, String sortBy, boolean isAscending, AsyncCallback<List<ProductOrderDTO>> asyncCallback);

    void updateProductOrder(String productOrderId, PRODUCTORDER_STATUS productorderStatus, Date deliveryTime, AsyncCallback<Void> async);

    void createProductOrder(String orderId, String title, String description, String aoiWKT, String selectionWKT, String productId, AsyncCallback<Void> asyncCallback);

    void createNewOrder(String title, String description, String userName, AsyncCallback<String> asyncCallback);

    void createSample(String productOrderId, String title, String description, String keyWords, AsyncCallback<String> callback);

    void loadSamples(int start, int limit, String sortBy, boolean isAscending, AsyncCallback<List<SampleDTO>> asyncCallback);

    void updateSample(SampleDTO sample, AsyncCallback<Void> asyncCallback);

    void removeSample(String id, AsyncCallback<Void> asyncCallback);

    void changeOrder(String productOrderId, String orderId, Boolean copyProduct, AsyncCallback<String> callback);

    void downloadProductHttp(String id, String url, String userName, String userPassword, AsyncCallback<Void> callback);

    void downloadProductFTP(String id, String host, String directory, String userName, String userPassword, String fileName, AsyncCallback<Void> callback);

    void addCreditTransaction(String username, TRANSACTION_TYPE transactionType, double amount, String currency, String comment, AsyncCallback<Void> asyncCallback);

    void loadAccountValues(AsyncCallback<String> stringAsyncCallback);
}
