package com.geocento.webapps.earthimages.emis.application.server.services;

import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.application.client.services.ProxyService;
import org.apache.log4j.Logger;

/**
 * Created by thomas on 27/08/14.
 */
public class ProxyServiceImpl extends ProxyCompatibleRemoteServiceServlet implements ProxyService {

    public ProxyServiceImpl() {
        logger = Logger.getLogger(ProxyServiceImpl.class);
        logger.info("Starting the proxy service");
    }

    @Override
    public String proxyOWSRequest(String serviceUrl, String service, String request, String parameters) throws EIException {
        // make sure call is allowed
        ServerUtil.validateUser(getThreadLocalRequest());
        String targetUrl = serviceUrl + "service=" + service + "&request=" + request + (parameters == null ? "" : "&" + parameters);
        try {
            return ServerUtil.getUrlData(targetUrl);
        } catch (Exception e) {
            throw new EIException("Communication error");
        }
    }

}