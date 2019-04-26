package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.UserOrderParameter;
import com.geocento.webapps.earthimages.emis.application.share.ProductRequestDTO;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.TYPE;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.*;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.map.implementation.MapPanel;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 20/04/2015.
 */
public class ProductHelper {

    public static String getProductColor(Product value) {
        return value.getType() == TYPE.ARCHIVE ?
                "#cc3333"
                : "#cccc33";
    }

    public static EOBounds getBounds(Product value) throws Exception {
        return EOBounds.getBounds(getCoordinates(value));
    }

    public static EOLatLng getCenter(Product product) throws Exception {
        return EOLatLng.parseWKT(StringUtils.extract(product.getCenter(), "((", "))"))[0];
    }

    public static String getDescription(Product product) {
        return product.getSatelliteName() + " " + product.getInstrumentName() + " " + (product.getModeName() == null ? "" : (product.getModeName().startsWith("0.00") ? "" : product.getModeName())) + (product.getStart() != null ? (" on " + DateUtil.displaySimpleUTCDate(product.getStart())) : "");
    }

    public static EOLatLng[] getCoordinates(Product product) throws Exception {
        return getCoordinates(product.getCoordinatesWKT());
    }

    public static EOLatLng[] getCoordinates(String wktString) throws Exception {
        return EOLatLng.parseWKT(StringUtils.extract(wktString, "((", "))"));
    }

    public static ProductDisplay convertToProductDisplay(Product product, AOI aoi) throws Exception {
        ProductDisplay productDisplay = new ProductDisplay();
        productDisplay.setAoi(aoi);
        productDisplay.setProduct(product);
        productDisplay.setCoordinates(getCoordinates(product));
        productDisplay.setBounds(EOBounds.getBounds(productDisplay.getCoordinates()));
        productDisplay.setCenter(productDisplay.getBounds().getCenter());
        productDisplay.setShowInfo(false);
        productDisplay.setVisible(true);
        productDisplay.setImageLoading(false);
        productDisplay.setDisplayImage(false);
        return productDisplay;
    }

    public static void updateProductRequestPrices(ProductRequestDTO productRequest) {
        // update the product request prices based on selection and options
        ProductPolicy specificPolicy = productRequest.getPolicy();
        Product product = productRequest.getProduct();
        // check product is still valid
        int intervalDays = (int) Math.abs((product.getStart().getTime() - new Date().getTime()) / DateUtil.dayInMs);
        PricingPolicy timePricingPolicy = getIntervalPricingPolicy(specificPolicy.getTimeToAcquisition(), intervalDays + "");
        if(timePricingPolicy == null) {
            productRequest.setOrderable(false);
            productRequest.setErrorMessage("This product is out of date, please remove");
            return;
        }
        productRequest.setCurrency(timePricingPolicy.getCurrency());
        // update image price
        Price imagePrice = null;
        double area = MapPanel.getPathArea(productRequest.getSelectionGeometry());
        switch(productRequest.getSelectionType()) {
            case frame: {
                int frameNumbers = productRequest.getProduct().getFrameNumbers();
                if (frameNumbers == 0) {
                    // calculate frame size
                    // get the side path first
                    try {
                        double sideLength = com.geocento.webapps.earthimages.emis.application.client.widgets.MapPanel.getSideLength(productRequest.getSelectionGeometry());
                        double ratio = sideLength / specificPolicy.getFrameSelectionPolicy().getFrameSize();
                        // take some 10% extra out for margin
                        ratio *= 0.9;
                        frameNumbers = Math.max(1, (int) Math.ceil(ratio));
                        productRequest.getProduct().setFrameNumbers(frameNumbers);
                    } catch (Exception e) {
                    }
                }
                // update price
                com.metaaps.webapps.earthimages.extapi.server.domain.price.Price pricePerFrame = specificPolicy.getFrameSelectionPolicy().getPricePerFrame();
                imagePrice = new Price(pricePerFrame.getValue() * frameNumbers, pricePerFrame.getCurrency());
            } break;
            case frameportion: {
                FramePortionSelectionPolicy framePortionSelectionPolicy = specificPolicy.getFramePortionSelectionPolicy();
                // calculate min area required for a valid configuration
                area = Math.max(framePortionSelectionPolicy.getMinArea(), area);
                imagePrice = new Price(framePortionSelectionPolicy.getPricePerSqkms().getValue() * area / 1000 / 1000, framePortionSelectionPolicy.getPricePerSqkms().getCurrency());
            } break;
            case shape: {
                com.metaaps.webapps.earthimages.extapi.server.domain.price.Price pricePerSqms = specificPolicy.getPolygonSelectionPolicy().getPricePerSqms();
                imagePrice = new Price(pricePerSqms.getValue() * area / 1000 / 1000, pricePerSqms.getCurrency());
            } break;
        }
        productRequest.setImagePrice(imagePrice.getValue());
        // update time price
        productRequest.setTimePrice(getPolicyPrice(timePricingPolicy, productRequest.getImagePrice(), area));
        // update total price
        double totalPrice = productRequest.getImagePrice();
        totalPrice += productRequest.getTimePrice();
        List<UserOrderParameter> parametersValues = productRequest.getOrderingOptions();
        if(parametersValues != null) {
            for(final UserOrderParameter userOrderParameter : parametersValues) {
                // find matching order parameter pricing policy
                OrderParameter orderParameter = ListUtil.findValue(specificPolicy.getParameters(), new ListUtil.CheckValue<OrderParameter>() {
                    @Override
                    public boolean isValue(OrderParameter value) {
                        return value.getId() == userOrderParameter.getOrderParameterId();
                    }
                });
                if(orderParameter != null && userOrderParameter.getPropertyValue() != null) {
                    PricingPolicy pricingPolicy = ProductHelper.getPricingPolicy(orderParameter, userOrderParameter.getPropertyValue());
                    if(pricingPolicy != null) {
                        double price = getPolicyPrice(pricingPolicy, productRequest.getImagePrice(), area);
                        userOrderParameter.setPrice(new Price(price, pricingPolicy.getCurrency()));
                    }
                }
                totalPrice += userOrderParameter.getPrice() == null ? 0 : userOrderParameter.getPrice().getValue();
            }
        }
        UserOrderParameter licensingOption = productRequest.getLicenseOption();
        if(licensingOption != null && licensingOption.getPropertyValue() != null) {
            OrderParameter orderParameter = specificPolicy.getLicensingPolicy().getParameter();
            if(orderParameter != null) {
                PricingPolicy pricingPolicy = ProductHelper.getPricingPolicy(orderParameter, licensingOption.getPropertyValue());
                if(pricingPolicy != null) {
                    double price = getPolicyPrice(pricingPolicy, totalPrice, area);
                    licensingOption.setPrice(new Price(price, pricingPolicy.getCurrency()));
                }
            }
            totalPrice += licensingOption.getPrice() == null ? 0 : licensingOption.getPrice().getValue();
        }
        productRequest.setTotalPrice(totalPrice);
        // set the converted price as well
        Price convertedTotalPrice = new Price(totalPrice, productRequest.getCurrency());
        try {
            convertedTotalPrice = com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getConvertedPrice(convertedTotalPrice);
        } catch (EIException e) {
        }
        productRequest.setConvertedTotalPrice(convertedTotalPrice);
    }

    public static Price getImagePrice(ProductRequestDTO productRequest) {
        Price imagePrice = null;
        ProductPolicy specificPolicy = productRequest.getPolicy();
        switch(productRequest.getSelectionType()) {
            case frame: {
                int frameNumbers = productRequest.getProduct().getFrameNumbers();
                if (frameNumbers == 0) {
                    // calculate frame size
                    // get the side path first
                    try {
                        double sideLength = com.geocento.webapps.earthimages.emis.application.client.widgets.MapPanel.getSideLength(productRequest.getSelectionGeometry());
                        double ratio = sideLength / specificPolicy.getFrameSelectionPolicy().getFrameSize();
                        // take some 10% extra out for margin
                        ratio *= 0.9;
                        frameNumbers = Math.max(1, (int) Math.ceil(ratio));
                        productRequest.getProduct().setFrameNumbers(frameNumbers);
                    } catch (Exception e) {
                    }
                }
                // update price
                com.metaaps.webapps.earthimages.extapi.server.domain.price.Price pricePerFrame = specificPolicy.getFrameSelectionPolicy().getPricePerFrame();
                imagePrice = new Price(pricePerFrame.getValue() * frameNumbers, pricePerFrame.getCurrency());
            } break;
            case frameportion: {
                FramePortionSelectionPolicy framePortionSelectionPolicy = specificPolicy.getFramePortionSelectionPolicy();
                // calculate min area required for a valid configuration
                double area = MapPanel.getPathArea(productRequest.getSelectionGeometry());
                area = Math.max(framePortionSelectionPolicy.getMinArea(), area);
                imagePrice = new Price(framePortionSelectionPolicy.getPricePerSqkms().getValue() * area / 1000 / 1000, framePortionSelectionPolicy.getPricePerSqkms().getCurrency());
            } break;
            case shape: {
                double area = MapPanel.getPathArea(productRequest.getSelectionGeometry());
                com.metaaps.webapps.earthimages.extapi.server.domain.price.Price pricePerSqms = specificPolicy.getPolygonSelectionPolicy().getPricePerSqms();
                imagePrice = new Price(pricePerSqms.getValue() * area / 1000 / 1000, pricePerSqms.getCurrency());
            } break;
        }
        return imagePrice;
    }

    private static PricingPolicy getIntervalPricingPolicy(IntervalPricingPolicy intervalPricingPolicy, String value) {
        Map<Double, PricingPolicy> policies = intervalPricingPolicy.getPolicies();
        try {
            // use the toString method and parse to convert
            // if the value cannot be converted, return null
            double convertedValue = Double.parseDouble(value.toString());
            if (convertedValue < intervalPricingPolicy.getMinValue()) {
                return null;
            }
            Double upperValue = null;
            for (Double boundValue : policies.keySet()) {
                if (upperValue == null) {
                    if (boundValue - convertedValue > 0) {
                        upperValue = boundValue;
                    }
                } else {
                    // look for the nearest and above
                    if (boundValue - convertedValue > 0 && boundValue - convertedValue < upperValue - convertedValue) {
                        upperValue = boundValue;
                    }
                }
            }
            // no upper limit was found
            if (upperValue == null) {
                return null;
            }
            return policies.get(upperValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double getPolicyPrice(PricingPolicy pricingPolicy, double imagePrice, double area) {
        switch(pricingPolicy.getType()) {
            case FIXED:
                return pricingPolicy.getValue();
            case PERCENTAGE:
                return pricingPolicy.getValue() / 100 * imagePrice;
            case AREA:
                return pricingPolicy.getValue() * (area / 1000.0 / 1000.0);
        }
        return 0;
    }

    public static PricingPolicy getPricingPolicy(OrderParameter orderParameter, String value) {
        if(value == null) {
            return null;
        }
        if(orderParameter instanceof ChoiceOrderParameter) {
            ConditionalPricingPolicy pricingPolicy = ((ChoiceOrderParameter) orderParameter).getPricingPolicy();
            ConditionalPricingCase conditionalPricingCase = ListUtil.findValue(pricingPolicy.getCasesPolicies(), pricingCase -> pricingCase.getCaseValue().contentEquals((String) value));
            PricingPolicy policy = conditionalPricingCase == null ? null : conditionalPricingCase.getPricingPolicy();
            // if no matching case was found, use the default statement
            if (policy == null) {
                policy = pricingPolicy.getOtherPolicy();
            }
            return policy;
        } else if(orderParameter instanceof LicenseOrderParameter) {
            LicenseOption licenseOption = ListUtil.findValue(((LicenseOrderParameter) orderParameter).getLicenseOptions(), pricingCase -> pricingCase.getOption().contentEquals((String) value));
            PricingPolicy policy = licenseOption == null ? null : licenseOption.getPricingPolicy();
            return policy;
        } else if(orderParameter instanceof IntegerOrderParameter || orderParameter instanceof DoubleOrderParameter) {
            IntervalPricingPolicy intervalPricingPolicy = orderParameter instanceof IntegerOrderParameter ?
                    ((IntegerOrderParameter) orderParameter).getPricingPolicy() :
                    ((DoubleOrderParameter) orderParameter).getPricingPolicy();
            return getIntervalPricingPolicy(intervalPricingPolicy, value);
        } else if(orderParameter instanceof TextOrderParameter) {
            return null;
        }
        return null;
    }

}
