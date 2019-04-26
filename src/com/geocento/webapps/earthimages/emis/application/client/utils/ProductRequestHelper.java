package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.entities.UserOrderParameter;
import com.geocento.webapps.earthimages.emis.application.share.ProductRequestDTO;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.OrderParameter;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 27/01/2017.
 */
public class ProductRequestHelper {

    public static void validateProductRequest(ProductRequestDTO productRequest) throws ValidationException {
        // verify it is orderable
        checkOrderable(productRequest);
        // check selection
        if(productRequest.getSelectionType() == null || productRequest.getSelectionGeometry() == null) {
            throw new ValidationException("Select a region");
        }
        EOLatLng[] selectedGeometry = productRequest.getSelectionGeometry();
        PolicyHelper.validateGeometrySelection(productRequest.getPolicy(), productRequest.getSelectionType(), selectedGeometry);
    }

    public static void validateProductRequestOptions(ProductRequestDTO productRequest) throws ValidationException {
        UserOrderParameter licenseParameter = productRequest.getLicenseOption();
        if(licenseParameter == null || licenseParameter.getPropertyValue() == null) {
            throw new ValidationException("Please specify your license selection");
        }
        List<OrderParameter> orderParameters = productRequest.getPolicy().getParameters();
        List<UserOrderParameter> parameterValues = productRequest.getOrderingOptions();
        if(orderParameters == null || orderParameters.size() == 0) {
            return;
        }
        if(parameterValues == null || parameterValues.size() == 0) {
            throw new ValidationException("Please configure your ordering options");
        }
        // look for one parameter which hasn't been filled in
        OrderParameter value = ListUtil.findValue(orderParameters, orderParameter -> {
            return ListUtil.findValue(parameterValues, userOrderParameter -> {
                return userOrderParameter.getOrderParameterId().equals(orderParameter.getId()) && userOrderParameter.getPropertyValue() != null;
            }) == null;
        });
        if(value != null) {
            throw new ValidationException("You have unconfigured ordering options");
        }
    }

    public static void checkOrderable(ProductRequestDTO productRequest) throws ValidationException {
        // nothing to do, for now all products are orderable
        Date now = new Date();
        switch (productRequest.getProduct().getType()) {
            case ARCHIVE: {
                if (productRequest.getPolicy() == null || productRequest.getSelectionType() == null || productRequest.getSelectionGeometry() == null) {
                    throw new ValidationException("This product cannot be ordered anymore");
                }
            } break;
            case TASKING: {
                // add 1 day to now for the cut off date
                // TODO - add this to ordering policies
                Date cutOffDate = DateUtil.addDays(now, 1);
                if(productRequest.getPolicy() == null) {
                    throw new ValidationException("This product cannot be ordered anymore");
                }
                if(productRequest.getProduct().getStart().before(cutOffDate)) {
                    throw new ValidationException("This product cannot be ordered anymore. The cut off date for ordering has been passed");
                }
            } break;
        }
    }

}
