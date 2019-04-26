package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.application.client.event.WebSocketClosedEvent;
import com.geocento.webapps.earthimages.emis.application.client.event.WebSocketFailedEvent;
import com.geocento.webapps.earthimages.emis.common.client.places.CustomHistorian;
import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.application.share.websockets.ProductOrderNotification;
import com.geocento.webapps.earthimages.emis.application.share.websockets.WebSocketMessage;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import org.realityforge.gwt.websockets.client.WebSocket;
import org.realityforge.gwt.websockets.client.WebSocketListenerAdapter;

/**
 * Created by thomas on 08/05/2017.
 */
public class NotificationSocketHelper {

    static private NotificationSocketHelper instance;

    private WebSocket webSocket;

    private int attempts = 0;

    protected NotificationSocketHelper() {
    }

    static public NotificationSocketHelper getInstance() {
        if(instance == null) {
            instance = new NotificationSocketHelper();
        }
        return instance;
    }

    public void startMaybeNotifications() {
        if(webSocket != null) {
            return;
        }
        String baseUrl = CustomHistorian.getHostPageBaseURL();
        webSocket = WebSocket.newWebSocketIfSupported();
        if (null != webSocket) {
            webSocket.setListener( new WebSocketListenerAdapter() {
                @Override
                public void onOpen( final WebSocket webSocket ) {
                    attempts = 0;
                }

                @Override
                public void onMessage( final WebSocket webSocket, final String data ) {
                    // check the type of message
                    WebSocketMessage.WebSocketMessageMapper mapper = GWT.create(WebSocketMessage.WebSocketMessageMapper.class);
                    WebSocketMessage webSocketMessage = mapper.read(data);
                    switch (webSocketMessage.getType()) {
                        case productOrderNotification:
                            ProductOrderNotification productOrderNotification = webSocketMessage.getProductOrderNotification();
                            Application.clientFactory.getEventBus().fireEvent(new com.geocento.webapps.earthimages.emis.application.client.event.OrderNotification(productOrderNotification));
                            break;
                        case logout:
                            Window.alert("You have been signed out");
                            Window.Location.reload();
                            break;
                    }
                }

                @Override
                public void onClose(WebSocket webSocket, boolean wasClean, int code, String reason) {
                    // TODO - needs a strategy to restart the socket
                    NotificationSocketHelper.this.webSocket = null;
                    // normal close error code
                    if(code == 1000 || code == 1001) {
                        Application.clientFactory.getEventBus().fireEvent(new WebSocketClosedEvent());
                        startMaybeNotifications();
                    } else {
                        // keep trying
                        Application.clientFactory.getEventBus().fireEvent(new WebSocketFailedEvent());
                        attempts++;
                        new Timer() {

                            @Override
                            public void run() {
                                startMaybeNotifications();
                            }
                        }.schedule(3000 + (2000 * attempts));
                    }
                }
            } );
            String webSocketProtocol = baseUrl.startsWith("https") ? "wss://" : "ws://";
            webSocket.connect(webSocketProtocol + baseUrl.substring(baseUrl.indexOf("://") + 3) + "notifications");
        }
    }
}
