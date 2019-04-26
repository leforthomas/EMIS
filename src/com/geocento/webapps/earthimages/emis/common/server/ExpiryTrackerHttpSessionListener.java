package com.geocento.webapps.earthimages.emis.common.server;

import com.geocento.webapps.earthimages.emis.application.server.websocket.NotificationSocket;
import com.geocento.webapps.earthimages.emis.common.server.domain.UserSession;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import org.apache.log4j.Logger;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.IOException;

@WebListener
public class ExpiryTrackerHttpSessionListener implements HttpSessionListener {

    Logger logger = Logger.getLogger(ExpiryTrackerHttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent event) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        logger.info("HTTP session destroyed");
        HttpSession httpSession = event.getSession();
        String sessionID = httpSession.getId();
        // TODO - implement equivalent ones for the other applications?
        UserSession userSession = (UserSession) httpSession.getAttribute("userSession");
        if(userSession != null) {
            USER_ROLE userRole = userSession.getUserType();
            String userName = userSession.getUserName();
            logger.info("User signed off is " + userName);
            try {
                NotificationSocket.sendLogout(sessionID);
            } catch (IOException ex) {
            }
        }
    }
}
