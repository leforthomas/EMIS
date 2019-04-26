package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HubSpotAPIUtil {

    static private Logger logger = Logger.getLogger(HubSpotAPIUtil.class);

    private static String redirectUri = "https://example.com";
    private static String appId = "179420";
    private static String oauthCode = "bcac9ed6-e82e-4580-b86a-9d1d900fe465";
    private static String refreshCode = "ae751441-19d3-40d2-b897-6fe86905639f";
    private static String clientId = "378a8a77-cffe-493a-a14b-03bfa28a2c90";
    private static String clientSecret = "58ea8763-a2f7-4ac8-b254-dec0b9b1bba8";

    public static void createContact(User dbUser) throws Exception {
        HubSpotAPIUtil.createContact(Arrays.asList(
                new ContactProperty("email", dbUser.getEmail()),
                new ContactProperty("firstname", dbUser.getFirstName() + " " + dbUser.getLastName()),
                new ContactProperty("address", dbUser.getAddress()),
                new ContactProperty("phone", dbUser.getPhone()),
                new ContactProperty("country", dbUser.getCountryCode()),
                new ContactProperty("company", dbUser.getCompany())
        ));

    }

    private static class OAuthToken {
        String authorizationHeader;
        Date expiryDate;
    }

    private static OAuthToken oauthToken;

    public static ListContactsResponse listContacts() throws Exception {
        Invocation invocation = getTarget("/contacts/v1/lists/all/contacts/all")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildGet();
        Response response = invocation.submit().get();
        if (response.getStatus() >= 300) {
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + response.readEntity(String.class));
            throw new Exception("Error listing contacts, message is " + response.readEntity(String.class));
        }
        return response.readEntity(ListContactsResponse.class);
    }

    public static Contact createContact(List<ContactProperty> contactProperties) throws Exception {
        CreateContact createContact = new CreateContact();
        createContact.setProperties(contactProperties);
        Invocation invocation = getTarget("/contacts/v1/contact/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildPost(Entity.json(createContact));
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            if(response.getStatus() == 409) {
                // contact already exists so do nothing
            } else {
                String responseString = response.readEntity(String.class);
                logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
                throw new Exception("Error creating contact, message is " + responseString);
            }
        }
        return response.readEntity(Contact.class);
    }

    public static void deleteContact(int vid) throws Exception {
        Invocation invocation = getTarget("/contacts/v1/contact/vid/" + vid)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildDelete();
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String responseString = response.readEntity(String.class);
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
            throw new Exception("Error creating contact, message is " + responseString);
        }
    }

    public static void createTimelineEvent(CreateEvent createEvent) throws Exception {
        Invocation invocation = getTarget("/integrations/v1/" + Utils.getSettings().getHubSpotAPIId() + "/timeline/event")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildPut(Entity.json(createEvent));
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String responseString = response.readEntity(String.class);
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
            throw new Exception("Error creating timeline event, message is " + responseString);
        }
    }

    public static Contact getContactByEmail(String email) throws Exception {
        Invocation invocation = getTarget("/contacts/v1/contact/email/" + email + "/profile")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildGet();
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String responseString = response.readEntity(String.class);
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
            throw new Exception("Error getting contact by email, message is " + responseString);
        }
        return response.readEntity(Contact.class);
    }

    public static DealsResponse listDeals() throws Exception {
        Invocation invocation = getTarget("/deals/v1/deal/paged")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildGet();
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String responseString = response.readEntity(String.class);
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
            throw new Exception("Error getting deals, message is " + responseString);
        }
        return response.readEntity(DealsResponse.class);
    }

    public static DealContent getDeal(int dealId) throws Exception {
        Invocation invocation = getTarget("/deals/v1/deal/" + dealId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildGet();
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String responseString = response.readEntity(String.class);
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
            throw new Exception("Error getting deal, message is " + responseString);
        }
        return response.readEntity(DealContent.class);
    }

    public static DealContent createDeal(CreateDealRequest createDealRequest) throws Exception {
        Invocation invocation = getTarget("/deals/v1/deal/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getAuthorization())
                .buildPost(Entity.json(createDealRequest));
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String responseString = response.readEntity(String.class);
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
            throw new Exception("Error creating deal, message is " + responseString);
        }
        return response.readEntity(DealContent.class);
    }

    private static WebTarget getTarget(String path) throws MalformedURLException {
        return ignoreSSLClient()
                .target(new URL(new URL("https://api.hubapi.com"), path).toString());
                //.queryParam("hapikey", "feac804a-280e-4c26-9b70-c8e0b0f81a5d");
    }

    public static Client ignoreSSLClient() {

        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");

            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new java.security.SecureRandom());

            return ClientBuilder.newBuilder()
                    .sslContext(sslcontext)
                    .hostnameVerifier((s1, s2) -> true)
                    .build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ClientBuilder.newClient();
        }
    }

    public static String getAuthorization() throws Exception {
        if(oauthToken == null || oauthToken.expiryDate.before(new Date())) {
            // get new token
            oauthToken = refreshToken();
        }
        return "Bearer " + oauthToken.authorizationHeader;
    }

    private static OAuthToken refreshToken() throws Exception {
        Form oauthForm = new Form();
        oauthForm.param("grant_type", "refresh_token");
        oauthForm.param("refresh_token", Utils.getSettings().getHubSpotRefreshToken());
        oauthForm.param("redirect_uri", redirectUri);
        oauthForm.param("client_id", Utils.getSettings().getHubSpotClientId());
        oauthForm.param("client_secret", Utils.getSettings().getHubSpotClientSecret());
        Invocation invocation = getTarget("/oauth/v1/token")
                .request(MediaType.APPLICATION_JSON)
                .buildPost(Entity.entity(oauthForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String responseString = response.readEntity(String.class);
            logger.error("Error calling the Hubspot API, CODE " + response.getStatus() + " and message is " + responseString);
            throw new Exception("Error refreshing token, message is " + responseString);
        }
        RefreshTokenResponse refreshTokenResponse = response.readEntity(RefreshTokenResponse.class);
        OAuthToken oauthToken = new OAuthToken();
        oauthToken.authorizationHeader = refreshTokenResponse.access_token;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, refreshTokenResponse.expires_in);
        oauthToken.expiryDate = calendar.getTime();
        return oauthToken;
    }

}
