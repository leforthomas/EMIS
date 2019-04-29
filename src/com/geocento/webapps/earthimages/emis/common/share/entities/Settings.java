package com.geocento.webapps.earthimages.emis.common.share.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by thomas on 12/01/15.
 *
 * A DTO for passing the client application settings
 *
 */
@Entity
public class Settings implements Serializable {

    @Id
    @GeneratedValue
    Long id;

    @Column(length = 255)
    String applicationName;

    @Column(length = 255)
    String serverType;

    // API settings
    @Column(length = 1000)
    String eartimagesAPIURL;
    @Column(length = 1000)
    String earthimagesAPIToken;
    @Column(length = 1000)
    String EIProductCallbackURL;
    int maxDaysFuture;

    @Column(length = 1000)
    String publishAPI;
    @Column(length = 1000)
    String publishAPICallbackURL;

    int monitoringRefresh;
    int maxImageAlerts;

    // email settings
    boolean reportByEmail;
    @Column(length = 100)
    String emailFrom;
    @Column(length = 100)
    String emailServer;
    Integer emailPort;
    boolean SMTPS;
    @Column(length = 100)
    String emailAccount;
    @Column(length = 100)
    String emailPassword;

    @Column(length = 1000)
    String websiteUrl;

    // shared application settings
    // the max AoI size in sqmeters
    double maxArea;
    int maxPolygonPoints;
    @Column(length = 1000)
    String helpLink;
    @Column(length = 1000)
    String aboutUsURL;
    int queryLimit;
    @Column(length = 10000)
    String termsAndConditionsURL;
    @Column(length = 10000)
    String supportEmail;
    @Column(length = 1000)
    String WMSLayersUrl;
    @Column(length = 1000)
    String productServiceWMSURL;
    @Column(length = 1000)
    String orderEmail;
    @Column(length = 1000)
    String contactUsURL;
    @Column(length = 100)
    String contactInfoSales;

    // Braintree settings
    boolean braintreeSandbox;
    @Column(length = 1000)
    String braintreeCSE;
    @Column(length = 1000)
    String braintreePublicKey;
    @Column(length = 1000)
    String braintreePrivateKey;
    @Column(length = 1000)
    String braintreeMerchantId;
    // payments settings
    double minAmountEuros;

    @Column(length = 100)
    String planetAPIToken;
    Integer planetAPISubscriptionId;

    // Xero settings
    @Column(length = 1000)
    private String xeroPathToPrivateKey;
    @Column(length = 100)
    private String xeroPrivateKeyPassword;
    @Column(length = 100)
    private String xeroConsumerKey;
    @Column(length = 100)
    private String xeroConsumerSecret;
    @Column(length = 1000)
    private String xeroContactLink;
    @Column(length = 1000)
    private String xeroInvoiceLink;
    @Column(length = 100)
    private String xeroBrandingThemeID;

    // HubSpot settings
    @Column(length = 100)
    private String hubSpotAPIKey;
    @Column(length = 100)
    private String hubSpotAPIId;
    @Column(length = 100)
    private String hubSpotRefreshToken;
    @Column(length = 100)
    private String hubSpotClientId;
    @Column(length = 100)
    private String hubSpotClientSecret;
    @Column(length = 1000)
    private String hubSpotDealLink;

    @Column(length = 200)
    private String AWSKey;
    @Column(length = 200)
    private String AWSSecretKey;

    // slack integration
    @Column(length = 1000)
    private String slackClientID;
    @Column(length = 1000)
    private String slackClientSecret;
    @Column(length = 1000)
    private String slackVerificationToken;
    @Column(length = 1000)
    private String privateSlackHook;
    private String eventsAPIURL;

    public Settings() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public String getEartimagesAPIURL() {
        return eartimagesAPIURL;
    }

    public void setEartimagesAPIURL(String eartimagesAPIURL) {
        this.eartimagesAPIURL = eartimagesAPIURL;
    }

    public String getEarthimagesAPIToken() {
        return earthimagesAPIToken;
    }

    public void setEarthimagesAPIToken(String earthimagesAPIToken) {
        this.earthimagesAPIToken = earthimagesAPIToken;
    }

    public String getPublishAPI() {
        return publishAPI;
    }

    public void setPublishAPI(String publishAPI) {
        this.publishAPI = publishAPI;
    }

    public int getMonitoringRefresh() {
        return monitoringRefresh;
    }

    public void setMonitoringRefresh(int monitoringRefresh) {
        this.monitoringRefresh = monitoringRefresh;
    }

    public int getMaxImageAlerts() {
        return maxImageAlerts;
    }

    public void setMaxImageAlerts(int maxImageAlerts) {
        this.maxImageAlerts = maxImageAlerts;
    }

    public boolean isReportByEmail() {
        return reportByEmail;
    }

    public void setReportByEmail(boolean reportByEmail) {
        this.reportByEmail = reportByEmail;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailServer() {
        return emailServer;
    }

    public void setEmailServer(String emailServer) {
        this.emailServer = emailServer;
    }

    public Integer getEmailPort() {
        return emailPort;
    }

    public void setEmailPort(Integer emailPort) {
        this.emailPort = emailPort;
    }

    public boolean isSMTPS() {
        return SMTPS;
    }

    public void setSMTPS(boolean SMTPS) {
        this.SMTPS = SMTPS;
    }

    public String getEmailAccount() {
        return emailAccount;
    }

    public void setEmailAccount(String emailAccount) {
        this.emailAccount = emailAccount;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public double getMaxArea() {
        return maxArea;
    }

    public void setMaxArea(double maxArea) {
        this.maxArea = maxArea;
    }

    public int getMaxPolygonPoints() {
        return maxPolygonPoints;
    }

    public void setMaxPolygonPoints(int maxPolygonPoints) {
        this.maxPolygonPoints = maxPolygonPoints;
    }

    public String getHelpLink() {
        return helpLink;
    }

    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }

    public String getAboutUsURL() {
        return aboutUsURL;
    }

    public void setAboutUsURL(String aboutUsURL) {
        this.aboutUsURL = aboutUsURL;
    }

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

    public String getTermsAndConditionsURL() {
        return termsAndConditionsURL;
    }

    public void setTermsAndConditionsURL(String termsAndConditionsURL) {
        this.termsAndConditionsURL = termsAndConditionsURL;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public void setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
    }

    public String getWMSLayersUrl() {
        return WMSLayersUrl;
    }

    public void setWMSLayersUrl(String WMSLayersUrl) {
        this.WMSLayersUrl = WMSLayersUrl;
    }

    public String getOrderEmail() {
        return orderEmail;
    }

    public void setOrderEmail(String orderEmail) {
        this.orderEmail = orderEmail;
    }

    public String getContactUsURL() {
        return contactUsURL;
    }

    public void setContactUsURL(String contactUsURL) {
        this.contactUsURL = contactUsURL;
    }

    public String getContactInfoSales() {
        return contactInfoSales;
    }

    public void setContactInfoSales(String contactInfoSales) {
        this.contactInfoSales = contactInfoSales;
    }

    public String getPublishAPICallbackURL() {
        return publishAPICallbackURL;
    }

    public void setPublishAPICallbackURL(String publishAPICallbackURL) {
        this.publishAPICallbackURL = publishAPICallbackURL;
    }

    public String getProductServiceWMSURL() {
        return productServiceWMSURL;
    }

    public void setProductServiceWMSURL(String productServerWMSURL) {
        this.productServiceWMSURL = productServerWMSURL;
    }

    public String getEIProductCallbackURL() {
        return EIProductCallbackURL;
    }

    public void setEIProductCallbackURL(String EIProductCallbackURL) {
        this.EIProductCallbackURL = EIProductCallbackURL;
    }

    public int getMaxDaysFuture() {
        return maxDaysFuture;
    }

    public void setMaxDaysFuture(int maxDaysFuture) {
        this.maxDaysFuture = maxDaysFuture;
    }

    public boolean isBraintreeSandbox() {
        return braintreeSandbox;
    }

    public void setBraintreeSandbox(boolean braintreeSandbox) {
        this.braintreeSandbox = braintreeSandbox;
    }

    public String getBraintreeCSE() {
        return braintreeCSE;
    }

    public void setBraintreeCSE(String braintreeCSE) {
        this.braintreeCSE = braintreeCSE;
    }

    public String getBraintreePublicKey() {
        return braintreePublicKey;
    }

    public void setBraintreePublicKey(String braintreePublicKey) {
        this.braintreePublicKey = braintreePublicKey;
    }

    public String getBraintreePrivateKey() {
        return braintreePrivateKey;
    }

    public void setBraintreePrivateKey(String braintreePrivateKey) {
        this.braintreePrivateKey = braintreePrivateKey;
    }

    public String getBraintreeMerchantId() {
        return braintreeMerchantId;
    }

    public void setBraintreeMerchantId(String braintreeMerchantId) {
        this.braintreeMerchantId = braintreeMerchantId;
    }

    public double getMinAmountEuros() {
        return minAmountEuros;
    }

    public void setMinAmountEuros(double minAmountEuros) {
        this.minAmountEuros = minAmountEuros;
    }

    public String getPlanetAPIToken() {
        return planetAPIToken;
    }

    public void setPlanetAPIToken(String planetAPIToken) {
        this.planetAPIToken = planetAPIToken;
    }

    public Integer getPlanetAPISubscriptionId() {
        return planetAPISubscriptionId;
    }

    public void setPlanetAPISubscriptionId(Integer planetAPISubscriptionId) {
        this.planetAPISubscriptionId = planetAPISubscriptionId;
    }

    public String getXeroPathToPrivateKey() {
        return xeroPathToPrivateKey;
    }

    public void setXeroPathToPrivateKey(String xeroPathToPrivateKey) {
        this.xeroPathToPrivateKey = xeroPathToPrivateKey;
    }

    public String getXeroPrivateKeyPassword() {
        return xeroPrivateKeyPassword;
    }

    public void setXeroPrivateKeyPassword(String xeroPrivateKeyPassword) {
        this.xeroPrivateKeyPassword = xeroPrivateKeyPassword;
    }

    public String getXeroConsumerKey() {
        return xeroConsumerKey;
    }

    public void setXeroConsumerKey(String xeroConsumerKey) {
        this.xeroConsumerKey = xeroConsumerKey;
    }

    public String getXeroConsumerSecret() {
        return xeroConsumerSecret;
    }

    public void setXeroConsumerSecret(String xeroConsumerSecret) {
        this.xeroConsumerSecret = xeroConsumerSecret;
    }

    public String getXeroContactLink() {
        return xeroContactLink;
    }

    public void setXeroContactLink(String xeroContactLink) {
        this.xeroContactLink = xeroContactLink;
    }

    public String getXeroInvoiceLink() {
        return xeroInvoiceLink;
    }

    public void setXeroInvoiceLink(String xeroInvoiceLink) {
        this.xeroInvoiceLink = xeroInvoiceLink;
    }

    public String getXeroBrandingThemeID() {
        return xeroBrandingThemeID;
    }

    public void setXeroBrandingThemeID(String xeroBrandingThemeID) {
        this.xeroBrandingThemeID = xeroBrandingThemeID;
    }

    public String getAWSKey() {
        return AWSKey;
    }

    public void setAWSKey(String AWSKey) {
        this.AWSKey = AWSKey;
    }

    public String getAWSSecretKey() {
        return AWSSecretKey;
    }

    public void setAWSSecretKey(String AWSSecretKey) {
        this.AWSSecretKey = AWSSecretKey;
    }

    public String getHubSpotAPIKey() {
        return hubSpotAPIKey;
    }

    public void setHubSpotAPIKey(String hubSpotAPIKey) {
        this.hubSpotAPIKey = hubSpotAPIKey;
    }

    public String getHubSpotAPIId() {
        return hubSpotAPIId;
    }

    public void setHubSpotAPIId(String hubSpotAPIId) {
        this.hubSpotAPIId = hubSpotAPIId;
    }

    public String getHubSpotRefreshToken() {
        return hubSpotRefreshToken;
    }

    public void setHubSpotRefreshToken(String hubSpotRefreshToken) {
        this.hubSpotRefreshToken = hubSpotRefreshToken;
    }

    public String getHubSpotClientId() {
        return hubSpotClientId;
    }

    public void setHubSpotClientId(String hubSpotClientId) {
        this.hubSpotClientId = hubSpotClientId;
    }

    public String getHubSpotClientSecret() {
        return hubSpotClientSecret;
    }

    public void setHubSpotClientSecret(String hubSpotClientSecret) {
        this.hubSpotClientSecret = hubSpotClientSecret;
    }

    public String getHubSpotDealLink() {
        return hubSpotDealLink;
    }

    public void setHubSpotDealLink(String hubSpotDealLink) {
        this.hubSpotDealLink = hubSpotDealLink;
    }

    public String getSlackClientID() {
        return slackClientID;
    }

    public void setSlackClientID(String slackClientID) {
        this.slackClientID = slackClientID;
    }

    public String getSlackClientSecret() {
        return slackClientSecret;
    }

    public void setSlackClientSecret(String slackClientSecret) {
        this.slackClientSecret = slackClientSecret;
    }

    public String getSlackVerificationToken() {
        return slackVerificationToken;
    }

    public void setSlackVerificationToken(String slackVerificationToken) {
        this.slackVerificationToken = slackVerificationToken;
    }

    public String getPrivateSlackHook() {
        return privateSlackHook;
    }

    public void setPrivateSlackHook(String privateSlackHook) {
        this.privateSlackHook = privateSlackHook;
    }

    public String getEventsAPIURL() {
        return eventsAPIURL;
    }

    public void setEventsAPIURL(String eventsAPIURL) {
        this.eventsAPIURL = eventsAPIURL;
    }
}
