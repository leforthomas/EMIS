package com.geocento.webapps.earthimages.emis.application.server.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.earthimages.emis.application.share.CartProductDTO;
import com.geocento.webapps.earthimages.emis.application.share.ProductMetadataDTO;
import com.geocento.webapps.earthimages.emis.application.share.websockets.CreditUpdatedNotification;
import com.geocento.webapps.earthimages.emis.application.share.websockets.ProductOrderNotification;
import com.geocento.webapps.earthimages.emis.application.share.websockets.WebSocketMessage;
import com.geocento.webapps.earthimages.emis.application.share.websockets.WorkspaceProductPublishedNotification;
import com.geocento.webapps.earthimages.emis.common.server.domain.*;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.ProductPublisherAPIUtil;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.PublishAPIUtils;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.ProductMetadata;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.PublishProcessProducts;
import org.apache.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint(value = "/notifications", configurator = BaseCustomConfigurator.class)
public class NotificationSocket extends BaseNotificationSocket {

    static Logger logger = Logger.getLogger(NotificationSocket.class);

    static private ConcurrentHashMap<String, List<Session>> userSessions = new ConcurrentHashMap<String, List<Session>>();

    public NotificationSocket() {
        logger.info("Starting websocket handler");
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // set maximum time out to 30 minutes
        session.setMaxIdleTimeout(30 * 60 * 1000L);
        UserSession userSession = getUserSession();
        if(userSession == null) {
            sendLoggedOut(session);
            throw new IOException("Not signed in");
        }
        addUserSession(userSession.getUserName(), session);
        logger.info("Open session for user " + userSession.getUserName());
    }

    private void addUserSession(String userName, Session session) {
        super.addHttpSession(session);
        List<Session> sessions = userSessions.get(userName);
        if(sessions == null) {
            sessions = new ArrayList<Session>();
            userSessions.put(userName, sessions);
        }
        sessions.add(session);
    }

    private void removeUserSession(String userName, Session session) {
        super.removeHttpSession(session);
        List<Session> sessions = userSessions.get(userName);
        if(sessions == null) {
            return;
        }
        sessions.remove(session);
    }

    @OnMessage
    public String echo(String message) {
        return message + " (from your server)";
    }

    @OnError
    public void onError(Throwable t) {
        handleError(t);
    }

    @OnClose
    public void onClose(Session session) {
        UserSession userSession = getUserSession();
        if(userSession == null) {
            // TODO - find a strategy
            // we need a parallel process to remove sessions when the http session has expired
            return;
        }
        removeUserSession(userSession.getUserName(), session);
        logger.info("Close session for user " + userSession.getUserName());
    }

    static public void sendMessageToAll(WebSocketMessage webSocketMessage) throws JsonProcessingException {
        List<Session> sessions = new ArrayList<Session>();
        Collection<String> userNames = userSessions.keySet();
        for(String userName : userNames) {
            sessions.addAll(userSessions.get(userName));
        }
        sendSessionsMessage(sessions, webSocketMessage);
    }

    static public void sendMessage(String userName, WebSocketMessage webSocketMessage) throws JsonProcessingException {
        sendSessionsMessage(userSessions.get(userName), webSocketMessage);
    }

    static public void sendSessionsMessage(List<Session> sessions, WebSocketMessage webSocketMessage) throws JsonProcessingException {
        if(sessions == null) {
            return;
        }
        logger.debug("Sending message to " + sessions.size() + " sessions");
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(webSocketMessage);
        for(Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                // TODO - remove session?

            }
        }
    }

    static public void sendLogout(String sessionID) throws JsonProcessingException {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType(WebSocketMessage.TYPE.logout);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(webSocketMessage);
        sendHttpSessionMessage(sessionID, message);
    }

    private void sendLoggedOut(Session session) throws IOException {
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setType(WebSocketMessage.TYPE.logout);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(webSocketMessage);
        session.getBasicRemote().sendText(message);
    }


    public static void notifyProductOrderStatusChanged(ProductOrder productOrder) {
        try {
            ProductOrderNotification productOrderNotification = new ProductOrderNotification();
            productOrderNotification.orderId = productOrder.getEventOrder().getId();
            productOrderNotification.productId = productOrder.getId();
            productOrderNotification.status = productOrder.getStatus();
            productOrderNotification.publishStatus = productOrder.getPublicationStatus();
            try {
                if(productOrder.getPublishProductRequests().size() > 0) {
                    ProductPublishRequest publishRequest = productOrder.getPublishProductRequests().get(0);
                    PublishProcessProducts publishProducts = ProductPublisherAPIUtil.getProductsPublished(publishRequest.getPublishTaskId());
                    productOrderNotification.productServiceWMSUrl = Utils.getSettings().getProductServiceWMSURL().replace("$userName", publishProducts.getWorkspace());
                    // TODO - change to have a list of products for each publishing
                    ArrayList<ProductMetadataDTO> products = new ArrayList<ProductMetadataDTO>();
                    for (ProductMetadata productMetadata : publishProducts.getGeneratedProductMetadas()) {
                        products.add(PublishAPIUtils.convertProductMetadata(productMetadata, publishRequest));
                    }
                    productOrderNotification.publishedProducts = products;
                    productOrderNotification.thumbnailUrl = productOrder.getThumbnailURL();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            // send notification
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setType(WebSocketMessage.TYPE.productOrderNotification);
            webSocketMessage.setProductOrderNotification(productOrderNotification);
            // TODO - restrict to users which have subscribed to the event
            NotificationSocket.sendMessageToAll(webSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void notifyCartProductsChanged(String userName, List<CartProductDTO> cartProducts) {
        try {
            // send notification
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setType(WebSocketMessage.TYPE.cartProductsChanged);
            webSocketMessage.setCartProducts(cartProducts);
            NotificationSocket.sendMessage(userName, webSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void notifyWorkspaceProductPublished(Workspace workspace, ProductPublishRequest productPublishRequest) {
        try {
            WorkspaceProductPublishedNotification workspaceProductPublishedNotification = new WorkspaceProductPublishedNotification();
            workspaceProductPublishedNotification.workspaceId = workspace.getId();
            workspaceProductPublishedNotification.productPublishRequestId = productPublishRequest.getId();
            workspaceProductPublishedNotification.publishStatus = productPublishRequest.getStatus();
            String message = null;
            switch (productPublishRequest.getStatus()) {
                case Failed: {
                    message = "Failed to process product in workspace " + workspace.getName();
                } break;
                case Published: {
                    message = "Product processed in workspace " + workspace.getName();
                    try {
                        PublishProcessProducts publishProducts = ProductPublisherAPIUtil.getProductsPublished(productPublishRequest.getPublishTaskId());
                        workspaceProductPublishedNotification.productThumbnailUrl = productPublishRequest.getThumbnailURL();
                        workspaceProductPublishedNotification.productServiceWMSUrl = Utils.getSettings().getProductServiceWMSURL().replace("$userName", publishProducts.getWorkspace());
                        // TODO - change to have a list of products for each publishing
                        ArrayList<ProductMetadataDTO> products = new ArrayList<ProductMetadataDTO>();
                        for (ProductMetadata productMetadata : publishProducts.getGeneratedProductMetadas()) {
                            products.add(PublishAPIUtils.convertProductMetadata(productMetadata, productPublishRequest));
                        }
                        workspaceProductPublishedNotification.publishedProducts = products;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } break;
            }
            workspaceProductPublishedNotification.message = message;
            // send notification
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setType(WebSocketMessage.TYPE.workspaceProductPublished);
            webSocketMessage.setWorkspaceProductPublishedNotification(workspaceProductPublishedNotification);
            NotificationSocket.sendMessageToAll(webSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void notifyCreditsUpdated(User user) {
        try {
            Credit credit = user.getCredit();
            CreditUpdatedNotification creditUpdatedNotification = new CreditUpdatedNotification();
            creditUpdatedNotification.amount = credit.getCurrent();
            creditUpdatedNotification.currency = credit.getCurrency();
            // send notification
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setType(WebSocketMessage.TYPE.creditUpdated);
            webSocketMessage.setCreditUpdatedNotification(creditUpdatedNotification);
            NotificationSocket.sendMessageToAll(webSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void notifyOrderChanged(User user, EventOrder eventOrder) {
        try {
            // send notification
            WebSocketMessage webSocketMessage = new WebSocketMessage();
            webSocketMessage.setType(WebSocketMessage.TYPE.orderNotification);
            webSocketMessage.setOrderId(eventOrder.getId());
            NotificationSocket.sendMessage(user.getUsername(), webSocketMessage);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
