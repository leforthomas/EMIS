package com.geocento.webapps.earthimages.emis.common.server.utils.slack;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Created by thomas on 22/03/2017.
 */
public class SlackAPIHelper {

    static Logger logger = Logger.getLogger(SlackAPIHelper.class);

    static public void sendMessageWebhook(String webhook, String message) throws EIException {
        Client client = ClientBuilder.newClient();

        Message messagePayload = new Message();
        messagePayload.setText(message);
        Response response = client
                .target(webhook)
                .request()
                .post(Entity.entity(messagePayload, "application/json"));

        if(response.getStatus() >= 300) {
            logger.error("Error calling slack API, CODE " + response.getStatus() + " and message is " + response.readEntity(String.class));
            throw new EIException("Error calling slack API, message is " + response.readEntity(String.class));
        }
    }

    static public void sendMessageWebhookNoException(String webhook, String message) {
        try {
            sendMessageWebhook(webhook, message);
        } catch (Exception e) {
        }
    }
}
