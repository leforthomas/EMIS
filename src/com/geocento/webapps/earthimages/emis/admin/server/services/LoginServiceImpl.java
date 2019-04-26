package com.geocento.webapps.earthimages.emis.admin.server.services;

import com.geocento.webapps.earthimages.emis.admin.client.services.LoginService;
import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.server.utils.BCrypt;
import com.geocento.webapps.earthimages.emis.common.server.utils.EMF;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * Created by thomas on 9/01/15.
 */
public class LoginServiceImpl extends ProxyCompatibleRemoteServiceServlet implements LoginService {

    public LoginServiceImpl() {
        // start logger
        logger = Logger.getLogger(LoginServiceImpl.class);
        logger.info("Starting login service");
    }

    public LoginInfo getLoginInfo() throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        return getLoginInfo(findUser(logUserName));
    }

    @Override
    public LoginInfo login(String userName, String password) {
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, userName);
            if(user != null) {
                if(user.getUserRole() != USER_ROLE.ADMINISTRATOR) {
                    throw new EIException("Not allowed");
                }
                String hashFromDB = user.getPasswordHash();
                boolean valid = BCrypt.checkpw(password, hashFromDB);
                if(valid) {
                    ServerUtil.createUserSession(user, getThreadLocalRequest());
                    LoginInfo loginInfo = getLoginInfo(user);
                    try {
                        em.getTransaction().begin();
                        user.setLastLoggedIn(new Date());
                        em.getTransaction().commit();
                    } catch(Exception e) {
                        handleException(em, e);
                    }
                    return loginInfo;
                }
            }
        } catch(Exception e) {
            logger.error(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
        return null;
    }

    private User findUser(String userName) throws EIException {
        EntityManager em = null;
        try {
            em = EMF.get().createEntityManager();
            User user = em.find(User.class, userName);
            if(user == null || user.getUserRole() != USER_ROLE.ADMINISTRATOR) {
                throw new EIException("Not allowed");
            }
            return user;
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    private LoginInfo getLoginInfo(User user) {
        if (user != null) {
            LoginInfo loginInfo = new LoginInfo();
            loginInfo.setLoggedIn(true);
            loginInfo.setLastLoggedIn(user.getLastLoggedIn());
            loginInfo.setSessionId(ServerUtil.getUserSessionId(getThreadLocalRequest()));
            loginInfo.setUserName(user.getUsername());
            loginInfo.setUserRole(user.getUserRole());
            return loginInfo;
        }

        return null;
    }

    @Override
    public void logOut() {
        HttpSession session = getThreadLocalRequest().getSession(true);
        if(session != null) {
            session.invalidate();
        }
    }

    @Override
    public LoginInfo checkSessionId(String sessionId) throws EIException {
        // compare the sessionid with the one currently held by the server
        String userSessionId = ServerUtil.getUserSessionId(getThreadLocalRequest());
        if(userSessionId != null && userSessionId.contentEquals(sessionId)) {
            return getLoginInfo();
        } else {
            return null;
        }
    }

    private void handleException(EntityManager em, Exception e) throws EIException {
        if(em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        if(e instanceof EIException) {
            throw (EIException) e;
        }
        logger.error(e.getMessage());
    }

}