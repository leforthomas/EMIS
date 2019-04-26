package com.geocento.webapps.earthimages.emis.admin.client.services;

import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by thomas on 9/01/15.
 */
public interface LoginServiceAsync {

    void login(String userName, String password, AsyncCallback<LoginInfo> asyncCallback);

    void logOut(AsyncCallback<Void> asyncCallback);

    void checkSessionId(String sessionId, AsyncCallback<LoginInfo> asyncCallback);

}
