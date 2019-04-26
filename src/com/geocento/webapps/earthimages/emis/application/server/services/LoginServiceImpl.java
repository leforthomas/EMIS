package com.geocento.webapps.earthimages.emis.application.server.services;

import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.common.server.domain.Credit;
import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.server.utils.BCrypt;
import com.geocento.webapps.earthimages.emis.common.server.utils.EMF;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.LoginInfo;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.Settings;
import com.geocento.webapps.earthimages.emis.application.client.services.LoginService;
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
            if(user == null) {
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
            loginInfo.setCanOrder(user.isCanOrder());
            Credit credit = user.getCredit();
            if(credit != null) {
                loginInfo.setPrepaidValue(credit.getCurrent());
                loginInfo.setPrepaidCurrency(credit.getCurrency());
            }
            loginInfo.setRateTable(ServerUtil.getCurrencyRateTable());
            // calculate the min amount for an order in uer currency
            Settings settings = Utils.getSettings();
            double minAmountEuros = settings.getMinAmountEuros();
            if(minAmountEuros > 0) {
                Price minAmount = new Price(minAmountEuros, "EUR");
                // check user currency
                if(!credit.getCurrency().contentEquals("EUR")) {
                    // find the nearest round price in the user currency
                    try {
                        minAmount = ServerUtil.getCurrencyRateTable().getConvertedPrice(minAmount, credit.getCurrency());
                        // keep only the highest digit
                        int highestDigit = (int) Math.floor(Math.log10(minAmount.getValue()));
                        minAmount.setValue(((int) Math.ceil(minAmount.getValue() / Math.pow(10, highestDigit)) * Math.pow(10, highestDigit)));
                    } catch (EIException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                loginInfo.setMinAmount(minAmount);
            }
            loginInfo.setChargeVAT(user.isChargeVAT());
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
        // for some reason jetty 9.4 adds the node suffix to the session which creates a problem
        // TODO - add a try/catch block?
        String userSessionId = ServerUtil.getUserSessionId(getThreadLocalRequest());
        if(userSessionId != null && sessionId != null && sessionId.startsWith(userSessionId)) {
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