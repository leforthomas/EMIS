package com.geocento.webapps.earthimages.emis.common.client.utils;

import com.google.gwt.i18n.client.NumberFormat;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.PricingPolicy;
import com.metaaps.webapps.earthimages.extapi.server.domain.price.Price;

public class Utils extends com.metaaps.webapps.libraries.client.widget.util.Utils {

    static private NumberFormat fileSizeNumberFormat = NumberFormat.getFormat(".#");

    public static String displayFileSize(Long size) {
        if(size == null) {
            return "unknown";
        }
        if(size < 1000) {
            return size + " bytes";
        }
        if(size < 1000 * 1000) {
            return fileSizeNumberFormat.format(size / 1000.0) + " KB";
        }
        if(size < 1000 * 1000 * 1000) {
            return fileSizeNumberFormat.format(size / 1000.0 / 1000.0) + " MB";
        }
        return fileSizeNumberFormat.format(size / 1000.0 / 1000.0 / 1000.0) + " GB";
    }

    public static String displayPrice(Price price) {
        return NumberFormat.getCurrencyFormat(price.getCurrency()).format(price.getValue());
    }

    public static String displayPrice(com.geocento.webapps.earthimages.emis.common.share.entities.Price price) {
        if(price != null && price.getCurrency() != null) {
            return NumberFormat.getCurrencyFormat(price.getCurrency()).format(price.getValue());
        } else {
            return "Unknown";
        }
    }

    public static String displayRoundedPrice(com.geocento.webapps.earthimages.emis.common.share.entities.Price price) {
        return displayRoundedPrice(convertPrice(price));
    }

    private static Price convertPrice(com.geocento.webapps.earthimages.emis.common.share.entities.Price price) {
        if(price == null) {
            return null;
        }
        Price convertedPrice = new Price();
        convertedPrice.setValue(price.getValue());
        convertedPrice.setCurrency(price.getCurrency());
        return convertedPrice;
    }

    public static String displayRoundedPrice(double value, String currency) {
        Price price = new Price();
        price.setValue(value);
        price.setCurrency(currency);
        return displayRoundedPrice(price);
    }

    public static String displayRoundedPrice(Price price) {
        if(price != null) {
            int fractionDigits = price.getValue() > 10 ? 0 : price.getValue() > 1 ? 1 : 2;
            return NumberFormat.getCurrencyFormat(price.getCurrency()).overrideFractionDigits(fractionDigits).format(price.getValue());
        } else {
            return "Unknown";
        }
    }

    public static String displayPricingPolicy(PricingPolicy pricing) {
        String priceString = pricing.getValue() + " ";
        String currency = pricing.getCurrency();
        switch(pricing.getType()) {
            case AREA:
                priceString += currency + " per square km";
                break;
            case FIXED:
                priceString += currency;
                break;
            case FRAME:
                priceString += currency + " per frame";
                break;
            case PERCENTAGE:
                priceString += "% of image price";
                break;
            default:
                priceString += " unsuported policy type";
        }
        return priceString;
    }

    public static native void printLog(String message) /*-{
        $wnd['console'].log(message);
    }-*/;

}
