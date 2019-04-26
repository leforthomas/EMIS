package com.geocento.webapps.earthimages.emis.application.client.services;

import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
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
}
