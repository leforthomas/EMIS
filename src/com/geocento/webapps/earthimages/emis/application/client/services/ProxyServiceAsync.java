package com.geocento.webapps.earthimages.emis.application.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by thomas on 27/08/14.
 */
public interface ProxyServiceAsync {
    void proxyOWSRequest(String serviceUrl, String service, String request, String version, AsyncCallback<String> async);
}
