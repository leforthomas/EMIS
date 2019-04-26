package com.geocento.webapps.earthimages.emis.common.server;

import com.geocento.webapps.earthimages.emis.common.server.domain.Reporting;
import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.server.domain.UserSession;
import com.geocento.webapps.earthimages.emis.common.server.utils.BCrypt;
import com.geocento.webapps.earthimages.emis.common.server.utils.EMF;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.EILoginException;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.utils.RateTable;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerUtil extends com.metaaps.webapps.libraries.server.ServerUtil {
	
	static private Logger logger = Logger.getLogger(ServerUtil.class);
    private static ConcurrentLinkedQueue<TimerTask> futureTasks = new ConcurrentLinkedQueue<TimerTask>();
    private static Timer futureTasksTimer;
    private static RateTable currencyRateTable;

    private static UserSession getUserSession(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		return (UserSession) session.getAttribute("userSession");
	}

    public static UserSession createUserSession(User user, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        UserSession userSession = new UserSession(user);
        session.setAttribute("userSession", userSession);
        return userSession;
    }

    public static void setUserSession(HttpServletRequest request, UserSession userSession) {
		HttpSession session = request.getSession(true);
		session.setAttribute("userSession", userSession);
	}

	public static String getUserSessionId(HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		return session.getId();
	}

	public static String validateUser(HttpServletRequest request) throws EIException {
	    UserSession userSession = getUserSession(request);
	    if(userSession != null) {
	    	return userSession.getUserName();
	    } else {
	    	throw(new EILoginException());
	    }
	}
	
	public static String validateUserAdministrator(HttpServletRequest request) throws EIException {
        UserSession userSession = getUserSession(request);
		if(userSession != null && userSession.getUserType() == USER_ROLE.ADMINISTRATOR) {
			return userSession.getUserName();
		} else {
			throw new EIException("Not allowed");
		}
	}
	
	public static String validateUserOperator(HttpServletRequest request) throws EIException {
        UserSession userSession = getUserSession(request);
        if(userSession != null && userSession.getUserType() == USER_ROLE.ADMINISTRATOR || userSession.getUserType() == USER_ROLE.OPERATOR) {
            return userSession.getUserName();
		} else {
			throw new EIException("Not allowed");
		}
	}
	
	/*
	 * retrieves persisted list of users from list of users ids aggregated in one string with a regex
	 */
	static public List<User> getUsersFromString(EntityManager em, String usersNamesString, String regex) throws EIException {
		String[] userNames = usersNamesString.split(regex);
		// check users
		if(usersNamesString == null || usersNamesString.length() == 0) {
			throw new EIException("List of user names not valid!");
		}
		if(userNames.length > 100) {
			throw new EIException("Too many users requested at once");
		}
		try {
			// trim user names
			List<String> userNamesList = new ArrayList<String>();
			for(String userName : userNames) {
				userNamesList.add(userName.trim());
			}
			return getUsersFromListString(em, userNamesList);
		} catch(Exception e) {
			throw new EIException("Share Scenario Failed on Server");
		}
	}
	
	/*
	 * retrieves persisted list of users from list of users ids
	 */
	public static List<User> getUsersFromListUser(EntityManager em, List<User> users) {
		List<String> userNamesList = new ArrayList<String>();
		for(User user : users) {
			userNamesList.add(user.getUsername());
		}
		return getUsersFromListString(em, userNamesList);
	}
	
	/*
	 * retrieves persisted list of users from detached list of users
	 */
	public static List<User> getUsersFromListString(EntityManager em, List<String> userNamesList) {
		// check user names are valid
		if(userNamesList == null || userNamesList.size() == 0) {
			return new ArrayList<User>();
		}
		// now retrieve users
		TypedQuery<User> query = em.createQuery("select u from User u where u.username in :listOfUserNames", User.class);
		query.setParameter("listOfUserNames", userNamesList);
		return query.getResultList();
	}

	public static String generatePasswordHash(String password) {
		return BCrypt.hashpw(password, BCrypt.gensalt());
	}

    public static void addFutureTask(TimerTask task) {
        futureTasks.add(task);
        if(futureTasksTimer == null) {
            // every ten minutes
            long futureTasksTime = 10 * 60 * 1000L;
            futureTasksTimer = new Timer();
            futureTasksTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runFutureTasks();
                }
            }, futureTasksTime, futureTasksTime);
        }
    }

    public static void runFutureTasks() {
        for(TimerTask task : new ArrayList<TimerTask>(futureTasks)) {
            try {
                task.run();
                futureTasks.remove(task);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    public static void handleException(EntityManager em, Exception e, Logger logger) throws EIException {
        if(em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        if(e instanceof EIException) {
            throw (EIException) e;
        }
        logger.error(e.getMessage());
    }

    public static List<User> getUsersAdministrator() {
        EntityManager em = EMF.get().createEntityManager();
        TypedQuery<User> query = em.createQuery("Select u from User u where u.userRole = :userRole", User.class);
        query.setParameter("userRole", USER_ROLE.ADMINISTRATOR);
        return query.getResultList();
    }

    public static String validateUserApproved(HttpServletRequest request) throws EILoginException {
        UserSession userSession = getUserSession(request);
        if(userSession != null && userSession.getUserStatus() == USER_STATUS.APPROVED) {
            return userSession.getUserName();
        } else {
            throw(new EILoginException());
        }
    }

    public static String validateUserRegistered(HttpServletRequest request) throws EILoginException {
        UserSession userSession = getUserSession(request);
        if(userSession != null && (userSession.getUserStatus() == USER_STATUS.VERIFIED || userSession.getUserStatus() == USER_STATUS.APPROVED)) {
            return userSession.getUserName();
        } else {
            throw(new EILoginException());
        }
    }

    public static String getCountryString(String countryCode) {
        return new Locale("en", countryCode).getDisplayCountry();
    }

    /**
     *
     * from http://stackoverflow.com/questions/7097623/need-to-perform-a-reverse-dns-lookup-of-a-particular-ip-address-in-java
     *
     * Do a reverse DNS lookup to find the host name associated with an IP address. Gets results more often than
     * {@link java.net.InetAddress#getCanonicalHostName()}, but also tries the Inet implementation if reverse DNS does
     * not work.
     *
     * Based on code found at http://www.codingforums.com/showpost.php?p=892349&postcount=5
     *
     * @param ip The IP address to look up
     * @return   The host name, if one could be found, or the IP address
     */
    public static String getHostName(String ip) {
        String retVal = null;
        final String[] bytes = ip.split("\\.");
        if (bytes.length == 4)
        {
            try
            {
                final Hashtable<String, String> env = new Hashtable<String, String>();
                env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                final javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext(env);
                final String reverseDnsDomain = bytes[3] + "." + bytes[2] + "." + bytes[1] + "." + bytes[0] + ".in-addr.arpa";
                final javax.naming.directory.Attributes attrs = ctx.getAttributes(reverseDnsDomain, new String[]
                        {
                                "PTR",
                        });
                for (final javax.naming.NamingEnumeration<? extends javax.naming.directory.Attribute> ae = attrs.getAll(); ae.hasMoreElements();)
                {
                    final javax.naming.directory.Attribute attr = ae.next();
                    final String attrId = attr.getID();
                    for (final Enumeration<?> vals = attr.getAll(); vals.hasMoreElements();)
                    {
                        String value = vals.nextElement().toString();
                        // System.out.println(attrId + ": " + value);

                        if ("PTR".equals(attrId))
                        {
                            final int len = value.length();
                            if (value.charAt(len - 1) == '.')
                            {
                                // Strip out trailing period
                                value = value.substring(0, len - 1);
                            }
                            retVal = value;
                        }
                    }
                }
                ctx.close();
            }
            catch (final javax.naming.NamingException e)
            {
                // No reverse DNS that we could find, try with InetAddress
                System.out.print(""); // NO-OP
            }
        }

        if (null == retVal)
        {
            try
            {
                retVal = java.net.InetAddress.getByName(ip).getCanonicalHostName();
            }
            catch (final java.net.UnknownHostException e1)
            {
                retVal = ip;
            }
        }

        return retVal;
    }

    public static void updateUserSession(User dbUser, ServletContext servletContext) {
        // update user session
        for(HttpSession session : ContextListener.getSessionMap(servletContext).values()) {
            UserSession userSession = (UserSession) session.getAttribute("userSession");
            if(userSession != null && userSession.getUserName().contentEquals(dbUser.getUsername())) {
                userSession.setUserStatus(dbUser.getUserStatus());
            }
        }
    }

    public static void addReporting(Reporting.TYPE type, String title, String content) {
        // create reporting and store in database
        EntityManager em = EMF.get().createEntityManager();
        try {
            Reporting reporting = new Reporting(type, title, content, new Date());
            em.getTransaction().begin();
            em.persist(reporting);
            em.getTransaction().commit();
        } catch(Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Failed to save reporting, error is: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public static File getUserDocumentFile(String logUserName, String fileName) {
        File userDocumentDirectory = new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.baseDocumentsDirectory), logUserName);
        if(!userDocumentDirectory.exists()) {
            userDocumentDirectory.mkdirs();
        }
        return new File(userDocumentDirectory, fileName);
    }

    public static File getTempFile(String fileName) {
        return new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.baseTempFileDirectory), fileName);
    }

    public static String getUserDocumentPath(File file) throws UnsupportedEncodingException {
        return "./api/document/download/" + URLEncoder.encode(file.getName(), "UTF-8");
    }

    public static boolean isUserAdministrator(HttpServletRequest httpServletRequest) {
        UserSession userSession = getUserSession(httpServletRequest);
        return userSession != null && userSession.getUserType() == USER_ROLE.ADMINISTRATOR;
    }

    public static RateTable getCurrencyRateTable() {
        return currencyRateTable;
    }

    public static void setCurrencyRateTable(RateTable currencyRateTable) {
        ServerUtil.currencyRateTable = currencyRateTable;
    }

    public static String getServiceUrl(String path) throws MalformedURLException {
        return new URL(new URL(Utils.getSettings().getWebsiteUrl()), path).toString();
    }
}
