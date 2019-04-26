package com.geocento.webapps.earthimages.emis.admin.server.services;

import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.admin.server.publishapis.ProductDownloaderAPIUtil;
import com.geocento.webapps.earthimages.emis.admin.share.*;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.geocento.webapps.earthimages.emis.application.server.websocket.NotificationSocket;
import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.ContextListener;
import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.common.server.domain.*;
import com.geocento.webapps.earthimages.emis.common.server.mailing.MailContent;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.PublishAPIUtils;
import com.geocento.webapps.earthimages.emis.common.server.utils.EMF;
import com.geocento.webapps.earthimages.emis.common.server.utils.OrderHelper;
import com.geocento.webapps.earthimages.emis.common.server.utils.TransactionsHelper;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.PlanetAPI;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.geocento.webapps.earthimages.productdownloader.DOWNLOAD_TYPES;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.DownloadProductRequest;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.DownloadProductRequestResponse;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.methods.FTPConnectionProperties;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.methods.HTTPConnectionProperties;
import com.google.gson.Gson;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.Satellite;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.ProductPolicy;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 19/01/2015.
 */
public class AdministrationServiceImpl extends ProxyCompatibleRemoteServiceServlet implements AdministrationService {

    public AdministrationServiceImpl() {
        // start logger
        logger = Logger.getLogger(AdministrationServiceImpl.class);
        logger.info("Starting administration service");
    }

    @Override
    public List<Satellite> loadSensors() throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        return null;
    }

    @Override
    public List<UserDTO> loadUsers(int start, int limit, String sortBy, boolean isAscending, String keyWord) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            boolean hasKeyword = keyWord != null && keyWord.length() > 0;
            String orderBy = "";
            boolean lastDownload = false;
            if(sortBy != null) {
                orderBy = " ORDER BY " + (sortBy.contentEquals("username") ? "u.username" :
                        (sortBy.contentEquals("emailAddress") ? "u.email" : "u.lastLoggedIn"))
                        + " " + (isAscending ? "ASC" : "DESC");
            }
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u" +
                    (hasKeyword ? " where UPPER(u.username) LIKE UPPER(:keyword)" : "") +
                    orderBy
                    , User.class);
            if (hasKeyword) {
                query.setParameter("keyword", "%" + keyWord + "%");
            }
            query.setFirstResult(start);
            query.setMaxResults(limit);
            // TODO - implement search using the Lucene library?
            List<User> users = query.getResultList();
            if(users.size() == 0) {
                return new ArrayList<UserDTO>();
            }
            return ListUtil.mutate(users, new ListUtil.Mutate<User, UserDTO>() {
                @Override
                public UserDTO mutate(final User user) {
                    return convertUserDTO(user);
                }
            });
        } catch (Exception e) {
            handleException(em, e);
        } finally {
            if(em != null) {
                em.close();
            }
        }
        return null;
    }

    private UserDTO convertUserDTO(User user) {
        final UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setUserStatus(user.getUserStatus());
        userDTO.setUserRole(user.getUserRole());
        userDTO.setCompany(user.getCompany());
        userDTO.setEmail(user.getEmail());
        userDTO.setAddress(user.getAddress());
        userDTO.setPhone(user.getPhone());
        userDTO.setCountryCode(user.getCountryCode());
        userDTO.setLastLoggedIn(user.getLastLoggedIn());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setDomain(user.getDomain());
        userDTO.setUsage(user.getUsage());
        userDTO.setCanOrder(user.isCanOrder());
        if(user.getCredit() != null) {
            CreditDTO creditDTO = new CreditDTO();
            creditDTO.setAmount(user.getCredit().getCurrent());
            creditDTO.setCurrency(user.getCredit().getCurrency());
            userDTO.setCredit(creditDTO);
        }
        userDTO.setChargeVAT(user.isChargeVAT());
        userDTO.setNeedsVATNumber(user.isNeedsVATNumber());
        userDTO.setCommunityVATNumber(user.getCommunityVATNumber());
        userDTO.setRegisteredDate(user.getRegisteredDate());
        return userDTO;
    }

    @Override
    public void updateUser(UserDTO userDTO) throws EIException {
        String logUserName = ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        logger.info("User '" + userDTO.getUsername() + "' updated by " + logUserName);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User dbUser = em.find(User.class, userDTO.getUsername());
            if(dbUser == null) {
                throw new EIException("Unknown user " + userDTO.getUsername());
            }
            USER_STATUS previousStatus = dbUser.getUserStatus();
            dbUser.setUsername(userDTO.getUsername());
            if(!StringUtils.isEmpty(userDTO.getPassword())) {
                dbUser.setPasswordHash(ServerUtil.generatePasswordHash(userDTO.getPassword()));
            }
            dbUser.setUserStatus(userDTO.getUserStatus());
            dbUser.setUserRole(userDTO.getUserRole());
            dbUser.setCompany(userDTO.getCompany());
            dbUser.setAddress(userDTO.getAddress());
            dbUser.setPhone(userDTO.getPhone());
            dbUser.setEmail(userDTO.getEmail());
            dbUser.setCountryCode(userDTO.getCountryCode());
            dbUser.setFirstName(userDTO.getFirstName());
            dbUser.setLastName(userDTO.getLastName());
            dbUser.setDomain(userDTO.getDomain());
            dbUser.setUsage(userDTO.getUsage());
            dbUser.setCanOrder(userDTO.isCanOrder());
            dbUser.setChargeVAT(userDTO.isChargeVAT());
            dbUser.setNeedsVATNumber(userDTO.isNeedsVATNumber());
            dbUser.setCommunityVATNumber(userDTO.getCommunityVATNumber());
            if(dbUser.getCredit() == null || dbUser.getCredit().getCurrency() == null) {
                Credit credit = new Credit();
                credit.setCurrent(userDTO.getCredit().getAmount());
                credit.setCurrency(userDTO.getCredit().getCurrency());
                em.persist(credit);
                dbUser.setCredit(credit);
            }
            em.getTransaction().commit();
            if(previousStatus != dbUser.getUserStatus()) {
                // status has changed, send an email
                MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.CONSUMER);
                if(previousStatus == USER_STATUS.PREVERIFIED || previousStatus == USER_STATUS.VERIFIED) {
                    if(dbUser.getUserStatus() == USER_STATUS.SUSPENDED) {
                        mailContent.addTitle("Your EarthImages NEO User Account creation request was rejected");
                        mailContent.addLine("If you believe that you have a case for registration and use of this service please contact Geocento team at: earthimages@geocento.com");
                    } else {
                        mailContent.addTitle("Your EarthImages NEO User Account creation request was approved");
                        mailContent.addLine("Your EarthImages user account has been approved. You can now search and order products from the EarthImages portal. Please reload the EarthImages web page.");
                    }
                } else {
                    if(dbUser.getUserStatus() == USER_STATUS.SUSPENDED) {
                        mailContent.addTitle("EarthImages NEO User Account Suspended");
                        mailContent.addLine("Your EarthImages user account has been suspended. If you believe this is a mistake, please contact the Geocento team at: earthimages@geocento.com");
                    } else {
                        mailContent.addTitle("EarthImages User Account Approved");
                        mailContent.addLine("Your EarthImages user account has been approved. You can now download products from the EarthImages portal. Please reload the EarthImages web page.");
                    }
                }
                mailContent.sendEmail(dbUser, "Your EarthImages account");
                ServerUtil.updateUserSession(dbUser, getThreadLocalRequest().getServletContext());
            }
        } catch (Exception e) {
            handleException(em, e);
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public Settings loadSettingValues() throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        return Utils.getSettings();
    }

    @Override
    public void updateSettings(Settings settings) throws EIException {
        String logUserName = ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        logger.info("Settings modified by user " + logUserName);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            // make sure they have the same id
            settings.setId(Utils.getSettings().getId());
            em.merge(settings);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

    }

    @Override
    public String loadLogFiles() throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        FileAppender appender = (FileAppender) org.apache.log4j.Logger.getRootLogger().getAppender("file");
        File file = new File(appender.getFile());
        try {
            return FileUtils.readFileToString(file);
        } catch (IOException e) {
            throw new EIException(e.getMessage());
        }
    }

    @Override
    public void createUser(String username, String email, String password, String firstName, String lastName, String organisation, String countryCode, DOMAIN domain, USAGE usage, USER_ROLE userRole, USER_STATUS userStatus, String currency) throws EIException {
        String logUserName = ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        logger.info("User '" + username + "' created by " + logUserName);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(ServerUtil.generatePasswordHash(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCompany(organisation);
        user.setDomain(domain);
        user.setUsage(usage);
        user.setUserRole(userRole);
        user.setUserStatus(userStatus);
        user.setCountryCode(countryCode);
        // create default settings
        UserSettings userSettings = new UserSettings();
        userSettings.setOwner(user);
        userSettings.setOverlaysOpacity(0.9);
        userSettings.setProductsOpacity(0.15);
        user.setSettings(userSettings);
        // create user cart
        UserCart userCart = new UserCart();
        userCart.setOwner(user);
        user.setUserCart(userCart);
        // add default credit
        Credit credit = new Credit();
        credit.setCurrent(0);
        credit.setCurrency(currency);
        // do not set currency for now
        credit.setOwner(user);
        user.setCredit(credit);
        UserLayers userLayer = new UserLayers();
        userLayer.setOwner(user);
        user.setUserLayer(userLayer);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public void removeUser(String userName) throws EIException {
        String logUserName = ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        logger.info("User '" + userName + "' removed by " + logUserName);
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User dbUser = em.find(User.class, userName);
            if(dbUser == null) {
                throw new EIException("Could not find user with user name " + userName);
            }
            em.remove(dbUser);
            em.getTransaction().commit();
            // update user session
            for(HttpSession session : ContextListener.getSessionMap(getThreadLocalRequest().getServletContext()).values()) {
                UserSession userSession = (UserSession) session.getAttribute("userSession");
                if(userSession != null && userSession.getUserName().contentEquals(dbUser.getUsername())) {
                    session.invalidate();
                }
            }
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public void testEmail() throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.ADMIN);
        mailContent.addLine("Test email successful");
        mailContent.sendEmail(ServerUtil.getUsersAdministrator(), "Test email");
    }

    @Override
    public List<ProductFetchTaskDTO> loadProductFetchTasks(int start, int limit, String sortBy, boolean isAscending) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            sortBy = sortBy == null ? "fetchDate" : sortBy;
            String orderLabel = null;
            switch (sortBy) {
                case "taskId": {
                    orderLabel = "id";
                } break;
                case "productOrderId": {
                    orderLabel = "productOrder.id";
                } break;
                case "orderId": {
                    orderLabel = "productOrder.order.id";
                } break;
                case "publishTaskId": {
                    orderLabel = "publishTaskId";
                } break;
                case "status": {
                    orderLabel = "status";
                } break;
                case "message": {
                    orderLabel = "statusMessage";
                } break;
                case "fetchDate":
                    default: {
                    orderLabel = "fetchDate";
                } break;
            }
            String orderBy = " order by p." + orderLabel + " " + (isAscending ? "ASC" : "DESC");
            TypedQuery<ProductFetchTask> query = em.createQuery("SELECT p FROM ProductFetchTask p where p.status in :statuses" +
                    orderBy,
                    ProductFetchTask.class);
            query.setParameter("statuses",
                    ListUtil.toList(new STATUS[]{
                            STATUS.created,
                            STATUS.planetCreated,
                            STATUS.requested,
                            STATUS.downloading,
                            STATUS.planetWaiting,
                            STATUS.downloadingFailed,
                            STATUS.downloadAvailable,
                            STATUS.planetDownloading,
                            STATUS.fetching,
                            STATUS.downloaded,
                            STATUS.published,
                            STATUS.publishing,
                            STATUS.publishingSuccess,
                            STATUS.publishingFailed,
                            STATUS.notPublished
                    }));
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<ProductFetchTask, ProductFetchTaskDTO>() {
                @Override
                public ProductFetchTaskDTO mutate(ProductFetchTask productFetchTask) {
                    ProductFetchTaskDTO productFetchTaskDTO = new ProductFetchTaskDTO();
                    productFetchTaskDTO.setId(productFetchTask.getId());
                    productFetchTaskDTO.setCompleted(productFetchTask.getCompleted());
                    productFetchTaskDTO.setCreated(productFetchTask.getCreated());
                    productFetchTaskDTO.setDownloadTaskId(productFetchTask.getDownloadTaskId());
                    productFetchTaskDTO.setPublishTaskId(productFetchTask.getPublishTaskId());
                    productFetchTaskDTO.setFetchDate(productFetchTask.getFetchDate());
                    productFetchTaskDTO.setProductOrderId(productFetchTask.getProductOrder().getId());
                    productFetchTaskDTO.setOrderId(productFetchTask.getProductOrder().getOrder().getId());
                    productFetchTaskDTO.setStatus(productFetchTask.getStatus());
                    productFetchTaskDTO.setStatusMessage(productFetchTask.getStatusMessage());
                    return productFetchTaskDTO;
                }
            });
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public void updateProductFetchTask(ProductFetchTaskDTO productFetchTaskDTO) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        if(productFetchTaskDTO == null) {
            throw new EIException("Product fetch task cannot be null");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            ProductFetchTask dbProductFetchTask = em.find(ProductFetchTask.class, productFetchTaskDTO.getId());
            // only do status for now
/*
            productFetchTaskDTO.setCompleted(productFetchTask.getCompleted());
            productFetchTaskDTO.setCreated(productFetchTask.getCreated());
            productFetchTaskDTO.setCreatedDate(productFetchTask.getCreatedDate());
            productFetchTaskDTO.setDownloadTaskId(productFetchTask.getDownloadTaskId());
            productFetchTaskDTO.setPublishTaskId(productFetchTask.getPublishTaskId());
            productFetchTaskDTO.setFetchDate(productFetchTask.getFetchDate());
            productFetchTaskDTO.setProductOrderId(productFetchTask.getProductOrder().getId());
*/
            dbProductFetchTask.setStatus(productFetchTaskDTO.getStatus());
/*
            productFetchTaskDTO.setStatusMessage(productFetchTask.getStatusMessage());
*/
            em.getTransaction().commit();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public List<ProductOrderDTO> loadProductOrders(int start, int limit, String sortBy, boolean isAscending) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            sortBy = sortBy == null ? "createdOn" : sortBy;
            String orderLabel = null;
            switch (sortBy) {
                case "orderId": {
                    orderLabel = "order.id";
                } break;
                case "orderName": {
                    orderLabel = "order.name";
                } break;
                case "status": {
                    orderLabel = "status";
                } break;
                case "createdOn": {
                    orderLabel = "creationTime";
                } break;
            }
            String orderBy = " order by p." + orderLabel + " " + (isAscending ? "ASC" : "DESC");
            TypedQuery<ProductOrder> query = em.createQuery("SELECT p FROM ProductOrder p " + // where p.status in :statuses" +
                            orderBy,
                    ProductOrder.class);
/*
            query.setParameter("statuses",
                    ListUtil.toList(new STATUS[]{
                            STATUS.requested
                    }));
*/
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return ListUtil.mutate(query.getResultList(), productOrder -> {
                return createProductOrderDTO(productOrder);
            });
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    private ProductOrderDTO createProductOrderDTO(ProductOrder productOrder) {
        ProductOrderDTO productOrderDTO = new ProductOrderDTO();
        productOrderDTO.setId(productOrder.getId());
        //productOrderDTO.setEIProductOrderId(productFetchTask.getEIProductOrderId());
        productOrderDTO.setStatus(productOrder.getStatus());
        productOrderDTO.setDescription(productOrder.getDescription());
        productOrderDTO.setGeocentoId(productOrder.getProductRequest() == null ? null : productOrder.getProductRequest().getGeocentoid());
        productOrderDTO.setOrderId(productOrder.getOrder().getId());
        productOrderDTO.setTitle(productOrder.getTitle());
        productOrderDTO.setCreated(productOrder.getCreationTime());
        return productOrderDTO;
    }

    @Override
    public void updateProductOrder(String productOrderId, PRODUCTORDER_STATUS productorderStatus, Date deliveryTime) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        if(productOrderId == null) {
            throw new EIException("Product order id cannot be null");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            productOrder.setStatus(productorderStatus);
            productOrder.setEstimatedDeliveryTime(deliveryTime);
            em.getTransaction().commit();
            NotificationSocket.notifyProductOrderStatusChanged(productOrder);
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }

/*
        // create a fake EI callback
        ProductOrderStatus productOrderStatus = new ProductOrderStatus();
        productOrderStatus.setProductOrderId(productOrderId);
        productOrderStatus.setStatus(com.metaaps.webapps.earthimages.extapi.server.domain.orders.PRODUCTORDER_STATUS.status.valueOf(productorderStatus.toString()));
        productOrderStatus.setDeliveryTime(deliveryTime);

        Client client = ClientBuilder.newClient();

        Invocation invocation = client.target(Utils.getSettings().getEIProductCallbackURL())
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.json(productOrderStatus));
        try {
            invocation.submit().get();
        } catch (Exception e) {
            throw new EIException("Error calling callback");
        }
*/
    }

    @Override
    public void createProductOrder(String orderId, String title, String description, String aoiWKT, String selectionWKT, String productId) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Order order = em.find(Order.class, orderId);
            if(order == null) {
                throw new EIException("Order with id #" + orderId + "does not exist");
            }
            boolean geocentoIds = productId.length() == 32;
            // deal with special case of Planet
            if(productId.startsWith("PL_")) {
                geocentoIds = true;
            }
            List<Product> products = EIAPIUtil.queryProductsById(productId, geocentoIds);
            if(products.size() == 0) {
                throw new EIException("No products found for the product id " + productId);
            }
            Product product = products.get(0);
            if(StringUtils.isEmpty(selectionWKT)) {
                selectionWKT = product.getCoordinatesWKT();
            }
            AOI aoi = null;
            if(aoiWKT != null && aoiWKT.length() > 5) {
                aoi = AOIUtils.fromWKT(aoiWKT);
            } else {
                aoi = AOIUtils.fromWKT(selectionWKT);
            }
            em.persist(aoi);
            ProductRequest productRequest = OrderHelper.createProductRequest(null, aoi, product);
            em.persist(productRequest);
            ProductOrder productOrder = OrderHelper.createProductOrder(productRequest);
            if(!StringUtils.isEmpty(title)) {
                productOrder.setTitle(title);
            }
            if(!StringUtils.isEmpty(description)) {
                productOrder.setDescription(description);
            }
            Long policyId = product.getPolicyId();
            if(policyId == null) {
                List<ProductPolicy> policies = EIAPIUtil.getCataloguePoliciesInstrument(ListUtil.toList(product.getInstrumentId()));
                if(policies.size() > 0) {
                    policyId = policies.get(0).getId();
                }
            }
            productOrder.setPolicyId(policyId);
            productOrder.setOrder(order);
            order.getProductOrders().add(productOrder);
            em.persist(productOrder);
            order.setLastUpdate(new Date());
            em.getTransaction().commit();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public String createNewOrder(String title, String description, String userName) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, userName);
            if(user == null) {
                throw new EIException("Unknown user " + userName);
            }
            em.getTransaction().begin();
            Order order = OrderHelper.createOrder(title, description);
            order.setOwner(user);
            user.getOrders().add(order);
            em.persist(order);
            em.getTransaction().commit();
            return order.getId();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public String createSample(String productOrderId, String title, String description, String keyWords) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            if(productOrder == null) {
                throw new EIException("Product order with id #" + productOrderId + " does not exist");
            }
            if(productOrder.getPublicationStatus() != PUBLICATION_STATUS.Published) {
                throw new EIException("Product order with id #" + productOrderId + " is not published yet");
            }
            // TODO - check the product sample has not been published twice
            ProductSample productSample = OrderHelper.createProductSample(productOrder);
            productSample.setTitle(title);
            productSample.setDescription(description);
            productSample.setKeywords(ListUtil.mutate(ListUtil.toList(keyWords.split(",")), value -> {return value.trim();}));
            em.persist(productSample);
            em.getTransaction().commit();
            return productSample.getId();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public List<SampleDTO> loadSamples(int start, int limit, String sortBy, boolean isAscending) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            sortBy = sortBy == null ? "sampleName" : sortBy;
            String orderLabel = null;
            switch (sortBy) {
                case "sampleId": {
                    orderLabel = "id";
                } break;
                case "sampleName": {
                    orderLabel = "title";
                } break;
                case "productOrderId": {
                    orderLabel = "productOrder.id";
                } break;
                default: {
                    orderLabel = "title";
                } break;
            }
            String orderBy = " order by p." + orderLabel + " " + (isAscending ? "ASC" : "DESC");
            TypedQuery<ProductSample> query = em.createQuery("select p from ProductSample p " + orderBy, ProductSample.class);
            query.setFirstResult(start);
            query.setMaxResults(limit);
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<ProductSample, SampleDTO>() {
                @Override
                public SampleDTO mutate(ProductSample sample) {
                    SampleDTO sampleDTO = new SampleDTO();
                    sampleDTO.setId(sample.getId());
                    sampleDTO.setName(sample.getTitle());
                    sampleDTO.setDescription(sample.getDescription());
                    sampleDTO.setKeywords(StringUtils.join(sample.getKeywords(), ","));
                    sampleDTO.setProductOrderDTO(createProductOrderDTO(sample.getProductOrder()));
                    return sampleDTO;
                }
            });
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public void updateSample(SampleDTO sample) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            ProductSample productSample = em.find(ProductSample.class, sample.getId());
            if(productSample == null) {
                throw new EIException("Product sample with id #" + sample.getId() + " does not exist");
            }
            productSample.setTitle(sample.getName());
            productSample.setDescription(sample.getDescription());
            productSample.setKeywords(ListUtil.toList(sample.getKeywords().split(",")));
            em.getTransaction().commit();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public void removeSample(String id) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            ProductSample productSample = em.find(ProductSample.class, id);
            productSample.setProductOrder(null);
            em.remove(productSample);
            em.getTransaction().commit();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public String changeOrder(String productOrderId, String orderId, Boolean copyProduct) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            ProductOrder originalProductOrder = em.find(ProductOrder.class, productOrderId);
            if(originalProductOrder.getOrder().getId().contentEquals(orderId)) {
                throw new EIException("Product order is already in order");
            }
            Order order = em.find(Order.class, orderId);
            if(order == null) {
                throw new EIException("Order does not exist");
            }
            // create new product order
            ProductOrder productOrder = OrderHelper.createProductOrder(originalProductOrder.getProductRequest());
            // now assign to new order
            productOrder.setOrder(order);
            order.getProductOrders().add(productOrder);
            // duplicate and publish new product order
            File originalProductFile = new File(originalProductOrder.getFileLocation());
            File productDirectory = OrderHelper.getProductDirectory(productOrder, true);
            File productFile = new File(productDirectory, originalProductFile.getName());
            FileUtils.copyFile(originalProductFile, productFile);
            productOrder.setFileLocation(productFile.getAbsolutePath());
            productOrder.setDeliveredTime(new Date());
            productOrder.setStatus(PRODUCTORDER_STATUS.Completed);
            em.persist(productOrder);
            // now start a product publishing task
            ProductFetchTask productFetchTask = PublishAPIUtils.createProductFetchTask(productOrder);
            productFetchTask.setStatus(STATUS.downloaded);
            em.persist(productFetchTask);
            // remove from previous order
            if(!copyProduct) {
                OrderHelper.removeProductOrder(em, originalProductOrder);
            }
            order.setLastUpdate(new Date());
            em.getTransaction().commit();
            NotificationSocket.notifyProductOrderStatusChanged(productOrder);
            return productOrder.getId();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public void downloadProductHttp(String id, String url, String userName, String userPassword) throws EIException {
        DownloadProductRequest downloadProductRequest = new DownloadProductRequest();
        downloadProductRequest.setDownloadType(DOWNLOAD_TYPES.HTTP);
        HTTPConnectionProperties httpConnectionProperties = new HTTPConnectionProperties();
        httpConnectionProperties.setUrl(url);
        httpConnectionProperties.setMethod("GET");
        httpConnectionProperties.setUserName(userName);
        httpConnectionProperties.setPassword(userPassword);
        downloadProductRequest.setHttpConnectionProperties(httpConnectionProperties);
        processRequest(id, downloadProductRequest);
    }

    @Override
    public void downloadProductFTP(String id, String host, String directory, String userName, String userPassword, String fileName) throws EIException {
        DownloadProductRequest downloadProductRequest = new DownloadProductRequest();
        downloadProductRequest.setDownloadType(DOWNLOAD_TYPES.FTP);
        FTPConnectionProperties ftpConnectionProperties = new FTPConnectionProperties();
        ftpConnectionProperties.setServer(host);
        ftpConnectionProperties.setDirectory(directory);
        ftpConnectionProperties.setFileName(fileName);
        ftpConnectionProperties.setUserName(userName);
        ftpConnectionProperties.setPassword(userPassword);
        downloadProductRequest.setFtpConnectionProperties(ftpConnectionProperties);
        processRequest(id, downloadProductRequest);
    }

    @Override
    public void addCreditTransaction(String username, TRANSACTION_TYPE transactionType, double amount, String currency, String comment) throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            // find user
            User user = em.find(User.class, username);
            TransactionsHelper.addUserTransaction(em, user, transactionType, amount, currency, comment, new Date());
            em.getTransaction().commit();
            // TODO - notify of update of credits
            NotificationSocket.notifyCreditsUpdated(user);
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            if(em != null) {
                em.close();
            }
        }
    }

    @Override
    public String loadAccountValues() throws EIException {
        ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        return new Gson().toJson(PlanetAPI.getAccountValues());
    }

    private void processRequest(String id, DownloadProductRequest downloadProductRequest) throws EIException {
        String logUserName = ServerUtil.validateUserAdministrator(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            ProductOrder productOrder = em.find(ProductOrder.class, id);
            if(productOrder == null) {
                throw new EIException("Could not find product order " + id);
            }
            downloadProductRequest.setSupplierId(productOrder.getProductRequest().getProviderId());
            downloadProductRequest.setReferenceId(productOrder.getId() + "");
            downloadProductRequest.setCallbackUrl(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.callBackUrlProductDownloader));
            DownloadProductRequestResponse response = ProductDownloaderAPIUtil.createTask(downloadProductRequest);
            if(response.getCode() >= 300) {
                throw new EIException("Issue submitting product fetch to downloader, message is " + response.getMessage());
            }
            ProductFetchTask productFetchTask = PublishAPIUtils.createProductFetchTask(productOrder);
            productFetchTask.setDownloadTaskId(response.getTaskId());
            productFetchTask.setStatus(STATUS.downloading);
            em.persist(productFetchTask);
            productOrder.setStatus(PRODUCTORDER_STATUS.InProduction);
            em.getTransaction().commit();
        } catch(Exception e) {
            ServerUtil.handleException(em, e, logger);
            throw new EIException(e instanceof EIException ? e.getMessage() : "Could not fetch product order");
        } finally {
            em.close();
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