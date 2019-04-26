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

import java.util.List;

@RemoteServiceRelativePath("customerservice")
public interface CustomerService extends RemoteService {

    ApplicationSettingsDTO loadApplicationSettings() throws EIException;

    List<UserLayerDTO> loadUserLayers() throws EIException;

    void deleteUserLayer(Long id) throws EIException;

    List<UserLayerDTO> addUserLayers(List<LayerDTO> selectedLayers) throws EIException;

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
