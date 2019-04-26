package com.geocento.webapps.earthimages.emis.application.server.services;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

public class ProxyCompatibleRemoteServiceServlet extends com.metaaps.webapps.libraries.server.servlets.ProxyCompatibleRemoteServiceServlet {

    protected Logger logger;

    @Override
    protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL, String strongName) {
        contextRelativePath = "/customer/";
        logger.info("load serialisation policy");
        logger.info(moduleBaseURL);
        logger.info(request.getContextPath() + contextRelativePath);
        return super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
    }

}
