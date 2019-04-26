package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.application.client.services.LoginService;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;

import java.util.Date;

/**
 * Created by thomas on 06/03/2015.
 */
public class Utils {

    // log-in session ID cookie duration
    public static long DURATION = 7 * DateUtil.dayInMs;

    static private String cookieSession = "EINEOID";

    private static LoginInfo loginInfo;

    static public void checkSignIn(final AsyncCallback<Void> callback) {
        // check if the user is logged in first
        String sessionId = Cookies.getCookie(cookieSession);
        if(sessionId != null) {
            LoginService.App.getInstance().checkSessionId(sessionId, new AsyncCallback<LoginInfo>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(LoginInfo result) {
                    if (result != null) {
                        loginInfo = result;
                    }
                    callback.onSuccess(null);
                }
            });
        } else {
            callback.onSuccess(null);
        }
    }

    public static LoginInfo getLoginInfo() {
        return loginInfo;
    }

    static public void saveSession(LoginInfo loginInfo, boolean extendedSession) {
        // remove cookie and login information
        if(loginInfo == null) {
            Cookies.removeCookie(cookieSession);
            Utils.loginInfo = null;
        } else {
            // set cookie if cookie was not present or has changed
            if(Cookies.getCookie(cookieSession) == null || !Cookies.getCookie(cookieSession).contentEquals(loginInfo.getSessionId())) {
                String sessionID = loginInfo.getSessionId();
                Date expires = new Date(new Date().getTime() + DURATION);
                Cookies.setCookie(cookieSession, sessionID, extendedSession ? expires : null, null, "/", false);
            }
        }
        Utils.loginInfo = loginInfo;
    }

    static public native void printLog(String message) /*-{
        $wnd['console'].log(message);
    }-*/;

    public static Price getConvertedPrice(Price totalPrice) throws EIException {
        if(loginInfo == null || loginInfo.getRateTable() == null) {
            return totalPrice;
        }
        return loginInfo.getRateTable().getConvertedPrice(totalPrice, loginInfo.getPrepaidCurrency());
    }
}
