package com.geocento.webapps.earthimages.emis.common.client.service;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * Created by thomas on 27/08/14.
 */
@RemoteServiceRelativePath("ProxyService")
public interface ProxyService extends RemoteService
{

    String proxyOWSRequest(String serviceUrl, String service, String request, String version) throws Exception;

    /**
     * Utility/Convenience class.
     * Use ProxyService.App.getInstance() to access static instance of ProxyServiceAsync
     */
    public static class App {
        private static final ProxyServiceAsync ourInstance = (ProxyServiceAsync) GWT.create(ProxyService.class);

        public static ProxyServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
