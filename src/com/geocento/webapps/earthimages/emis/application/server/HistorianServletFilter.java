package com.geocento.webapps.earthimages.emis.application.server;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HistorianServletFilter implements Filter {

    static String[] places = new String[] {"mapviewer"};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        if(requestURI.contains("/_")) {
            if(requestURI.matches("(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|jpeg|gif|png|js|css))(?:\\?([^#]*))?(?:#(.*))?")) {
                // remove the history part from the request
                int index = requestURI.indexOf("/_");
                String resourceRequest = requestURI.substring(index + 2);
                // remove the next two blocks of the path
                resourceRequest = resourceRequest.substring(resourceRequest.indexOf("/"));
                resourceRequest = resourceRequest.substring(resourceRequest.indexOf("/"));
                request.getRequestDispatcher(resourceRequest).forward(servletRequest, servletResponse);
            } else {
                request.getRequestDispatcher("/").forward(servletRequest, servletResponse);
            }
            return;
        }
        for(String place : places) {
            int index = requestURI.indexOf("/" + place + "/");
            if (index != -1) {
                requestURI = requestURI.substring(0, index + 1);
                request.getRequestDispatcher(requestURI).forward(servletRequest, servletResponse);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
