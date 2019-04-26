package com.geocento.webapps.earthimages.emis.application.server.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.earthimages.emis.application.share.websockets.WebSocketMessage;
import com.geocento.webapps.earthimages.emis.common.server.domain.UserSession;
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

}
