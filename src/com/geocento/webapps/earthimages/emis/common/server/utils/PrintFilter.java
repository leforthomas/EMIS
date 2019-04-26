package com.geocento.webapps.earthimages.emis.common.server.utils;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

public class PrintFilter implements ContainerResponseFilter, ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {


        System.out.println("Response body: " + responseContext.getEntity());
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String string = IOUtils.toString(requestContext.getEntityStream());
        System.out.println("request body: " + string);
    }


}