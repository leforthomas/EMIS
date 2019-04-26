package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;

/**
 * Created by thomas on 26/08/2015.
 */
public class ApplicationSettingsDTO implements Serializable {

    int queryLimit;
    double maxArea;
    String wmsURL;
    String aboutUsURL;
    String contactUs;
    String contactInfoSales;
    String applicationName;
    int maxDaysFuture;
    private String termsOfSalesUrl;

    public ApplicationSettingsDTO() {
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setMaxArea(double maxArea) {
        this.maxArea = maxArea;
    }

    public double getMaxArea() {
        return maxArea;
    }

    public String getWmsURL() {
        return wmsURL;
    }

    public void setWmsURL(String wmsURL) {
        this.wmsURL = wmsURL;
    }

    public String getAboutUsURL() {
        return aboutUsURL;
    }

    public void setAboutUsURL(String aboutUsURL) {
        this.aboutUsURL = aboutUsURL;
    }

    public String getContactUs() {
        return contactUs;
    }

    public void setContactUs(String contactUs) {
        this.contactUs = contactUs;
    }

    public String getContactInfoSales() {
        return contactInfoSales;
    }

    public void setContactInfoSales(String contactInfoSales) {
        this.contactInfoSales = contactInfoSales;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getMaxDaysFuture() {
        return maxDaysFuture;
    }

    public void setMaxDaysFuture(int maxDaysFuture) {
        this.maxDaysFuture = maxDaysFuture;
    }

    public String getTermsOfSalesUrl() {
        return termsOfSalesUrl;
    }

    public void setTermsOfSalesUrl(String termsOfSalesUrl) {
        this.termsOfSalesUrl = termsOfSalesUrl;
    }
}
