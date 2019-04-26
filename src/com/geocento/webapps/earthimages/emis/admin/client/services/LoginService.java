package com.geocento.webapps.earthimages.emis.admin.client.services;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Created by thomas on 9/01/15.
 */
@RemoteServiceRelativePath("loginservice")
public interface LoginService extends RemoteService {

    LoginInfo login(String userName, String password);
    void logOut();
    LoginInfo checkSessionId(String sessionId) throws EIException;

    /**
     * Utility/Convenience class.
     * Use LoginService.App.getInstance() to access static instance of LoginServiceAsync
     */
    public static class App {
        private static final LoginServiceAsync ourInstance = (LoginServiceAsync) GWT.create(LoginService.class);

        public static LoginServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
