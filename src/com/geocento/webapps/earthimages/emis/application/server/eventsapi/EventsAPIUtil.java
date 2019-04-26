package com.geocento.webapps.earthimages.emis.application.server.eventsapi;

import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.geocento.webapps.earthimages.emis.application.server.utils.APIUtils;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.google.gson.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.Satellite;
import com.metaaps.webapps.earthimages.extapi.server.domain.SearchRequest;
import com.metaaps.webapps.earthimages.extapi.server.domain.SearchResponse;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

public class EventsAPIUtil {

    static private Logger logger = Logger.getLogger(EventsAPIUtil.class);

    public static List<EventDTO> loadEvents() throws Exception {
        Client client = APIUtils.ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEventsAPIURL() + "search/")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String message = APIUtils.getErrorMessage(response);
            logger.error("Error calling events API, CODE " + response.getStatus() + " and message is " + message);
            throw new EIException("Error retrieving events, message is " + message);
        }
        return response.readEntity(new GenericType<List<EventDTO>>(){});
    }

    public static EventDTO loadEvent(String eventUID) throws Exception {
        Client client = APIUtils.ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEventsAPIURL() + "events/" + eventUID)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String message = APIUtils.getErrorMessage(response);
            logger.error("Error calling events API, CODE " + response.getStatus() + " and message is " + message);
            throw new EIException("Error retrieving events, message is " + message);
        }
        return response.readEntity(EventDTO.class);
    }

    private static String getToken() {
        return null;
    }

}
