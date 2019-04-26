package com.geocento.webapps.earthimages.emis.admin.server.servlets;

import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CheckLogginServlet extends HttpServlet {
	
	static private Logger logger = Logger.getLogger(CheckLogginServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
            // get the requesting user
            ServerUtil.validateUserAdministrator(request);
            // get the tokens with redirecting to authorise
            response.sendRedirect("/");
        } catch (Exception e) {
            // redirect to sign in page
            response.sendRedirect("#signin:");
        }
    }

}
