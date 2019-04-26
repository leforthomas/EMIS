package com.geocento.webapps.earthimages.emis.application.server.servlets;

import com.geocento.webapps.earthimages.emis.common.server.domain.ImageAlert;
import com.geocento.webapps.earthimages.emis.common.server.utils.EMF;
import com.geocento.webapps.earthimages.emis.common.server.utils.UtilImageAlert;
import com.geocento.webapps.earthimages.emis.common.share.EIException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by thomas on 24/11/2014.
 */
public class UnregisterAlertServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String emailAddress = request.getParameter("emailAddress");
            String hash = request.getParameter("hash");
            String imageAlertId = request.getParameter("imageAlert");
            // check the parameters match the hash
            if(hash == null || emailAddress == null || imageAlertId == null || !UtilImageAlert.verifyHash(imageAlertId, emailAddress, hash)) {
                throw new EIException("Invalid parameters");
            }
            // now try to find the image alert
            // get sensor filter
            EntityManager em = EMF.get().createEntityManager();
            ImageAlert imageAlert = em.find(ImageAlert.class, Long.parseLong(imageAlertId));
            if (imageAlert == null) {
                throw new EIException("Could not find image alert with id " + imageAlertId);
            }
            try {
                em.getTransaction().begin();
                em.remove(imageAlert);
                em.getTransaction().commit();
                response.getWriter().print("Image alert was successfully removed.");
            } catch (Exception e) {
                throw new EIException("Error when removing image alert, please try again.");
            } finally {
                em.close();
            }
        } catch (EIException e) {
            response.getWriter().print("Error when attempting to unregister image alert, reason is: " + e.getMessage());
        }
    }

}
