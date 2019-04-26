package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.google.gwt.user.client.Cookies;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;

import java.util.Date;

/**
 * Created by thomas on 22/09/2015.
 */
public class SettingsHelper {

    // used for signed in and non signed in users
    static protected String preferredCurrency = "USD";
    static protected double overlayTransparency = 1.0;
    static protected double productOpacity = 0.0;
    static protected double aoiOpacity = 0.15;
    static protected String baseMapId = "";
    private static double productSelectionOpacity = 0.15;
    private static SCREENMODE screenMode;
    private static boolean reducedDisplay;

    public static void changeDisplaySettings(double transparencyOverlays, double transparencyProduct, double transparencyObservation, boolean reducedDisplay) {
        overlayTransparency = transparencyOverlays;
        productOpacity = transparencyProduct;
        aoiOpacity = transparencyObservation;
        SettingsHelper.reducedDisplay = reducedDisplay;
        // update cookie
        Cookies.setCookie("overlayTransparency", overlayTransparency + "");
        Cookies.setCookie("productOpacity", productOpacity + "");
        Cookies.setCookie("aoiOpacity", aoiOpacity + "");
        Cookies.setCookie("redcuedDisplay", reducedDisplay + "");
    }

    public static void baseMapChanged(String mapId) {
        Cookies.setCookie("baseMapId", mapId);
    }

    public static void loadCookies() {
        if(Cookies.getCookie("overlayTransparency") != null) {
            overlayTransparency = Double.parseDouble(Cookies.getCookie("overlayTransparency"));
        }
        if(Cookies.getCookie("productOpacity") != null) {
            productOpacity = Double.parseDouble(Cookies.getCookie("productOpacity"));
        }
        if(Cookies.getCookie("aoiOpacity") != null) {
            aoiOpacity = Double.parseDouble(Cookies.getCookie("aoiOpacity"));
        }
        if(Cookies.getCookie("baseMapId") != null) {
            baseMapId = Cookies.getCookie("baseMapId");
        }
        if(Cookies.getCookie("reducedDisplay") != null) {
            try {
                reducedDisplay = Boolean.parseBoolean(Cookies.getCookie("reducedDisplay"));
            } catch(Exception e) {

            }
        }
        if(Cookies.getCookie("screenMode") != null) {
            try {
                screenMode = SCREENMODE.valueOf(Cookies.getCookie("screenMode"));
            } catch (Exception e) {

            }
        }
    }

    public static String getPreferredCurrency() {
        return preferredCurrency;
    }

    public static double getOverlayTransparency() {
        return overlayTransparency;
    }

    public static double getProductOpacity() {
        return productOpacity;
    }

    public static double getAoiOpacity() {
        return aoiOpacity;
    }

    public static String getBaseMapId() {
        return baseMapId;
    }

    public static double getProductSelectionOpacity() {
        return productSelectionOpacity;
    }

    public static void setProductSelectionOpacity(double productSelectionOpacity) {
        SettingsHelper.productSelectionOpacity = productSelectionOpacity;
        Cookies.setCookie("productSelectionOpacity", productSelectionOpacity + "", DateUtil.addDays(new Date(), 60));
    }

    public static void setPreferredCurrency(String preferredCurrency) {
        SettingsHelper.preferredCurrency = preferredCurrency;
        Cookies.setCookie("preferredCurrency", preferredCurrency + "", DateUtil.addDays(new Date(), 60));
    }

    public static void setOverlayTransparency(double overlayTransparency) {
        SettingsHelper.overlayTransparency = overlayTransparency;
        Cookies.setCookie("overlayTransparency", overlayTransparency + "", DateUtil.addDays(new Date(), 60));
    }

    public static void setProductOpacity(double productOpacity) {
        SettingsHelper.productOpacity = productOpacity;
        Cookies.setCookie("productOpacity", productOpacity + "", DateUtil.addDays(new Date(), 60));
    }

    public static void setAoiOpacity(double aoiOpacity) {
        SettingsHelper.aoiOpacity = aoiOpacity;
        Cookies.setCookie("aoiOpacity", aoiOpacity + "", DateUtil.addDays(new Date(), 60));
    }

    public static void setBaseMapId(String baseMapId) {
        SettingsHelper.baseMapId = baseMapId;
        Cookies.setCookie("baseMapId", baseMapId + "", DateUtil.addDays(new Date(), 60));
    }

    public static void setScreenMode(SCREENMODE screenMode) {
        SettingsHelper.screenMode = screenMode;
        Cookies.setCookie("screenMode", screenMode + "", DateUtil.addDays(new Date(), 60));
    }

    public static SCREENMODE getScreenMode() {
        return screenMode;
    }

    public static boolean isReducedDisplay() {
        return reducedDisplay;
    }

    public static void setReducedDisplay(boolean reducedDisplay) {
        SettingsHelper.reducedDisplay = reducedDisplay;
    }
}
