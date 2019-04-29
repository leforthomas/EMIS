package com.geocento.webapps.earthimages.emis.common.server; /**
 * Created by thomas on 23/07/2014.
 */

import com.geocento.webapps.earthimages.emis.application.server.websocket.NotificationSocket;
import com.geocento.webapps.earthimages.emis.common.server.domain.*;
import com.geocento.webapps.earthimages.emis.common.server.mailing.MailContent;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.MethodValues;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.ProductDownloaderAPIUtil;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.ProductPublisherAPIUtil;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.PublishAPIUtils;
import com.geocento.webapps.earthimages.emis.common.server.utils.*;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.geocento.webapps.earthimages.emis.common.share.utils.RateTable;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.PlanetAPI;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2.OrderStatus;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.DownloadProductRequest;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.DownloadProductRequestResponse;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.ProductDownloaderTaskDTO;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.ProductPublisherTaskDTO;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.PublishProcessProducts;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.PublishProductRequest;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.PublishProductRequestResponse;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.SearchRequest;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.*;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class ContextListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    private Logger logger;

    private static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MMM-dd 'at' HH:mm:ss 'UTC'");
    static {
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /*
        list of periodic services to run
     */
    private Timer imageAlertTimer;
    private Timer reportingTimer;
    private Timer tasksTimer;
    private Timer rateTableTimer;

    private static int MAX_THREADS = 5;
    private ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

    // Public constructor is required by servlet spec
    public ContextListener() {

        try {
            Configuration.loadConfiguration();

            // test entity manager configuration setup
            EntityManager em = EMF.get().createEntityManager();
            em.close();

            // srart logger
            logger = Logger.getLogger(ContextListener.class);
            logger.info("Starting context listener");

            // apply version fixes
            applyFixes();
            // reset the fetch tasks which have been finished in a non transitional state
            resetTasks();
            // start the automatic alert service timer
            startImageAlerts();
            startReportingTimer();
            startTasksService();
            startRateTable();
        } catch(Exception e) {
            System.out.println(e.getMessage());
            // stop servlet as the servlet failed to initialise correctly
            System.exit(0);
        }
        // send an email to say we have started the application
        MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.ADMIN);
        mailContent.addTitle("The " + Utils.getSettings().getApplicationName() + " server application started (" + Utils.getSettings().getServerType() + ")");
        try {
            mailContent.sendEmail(ServerUtil.getUsersAdministrator(), "Server started");
        } catch (EIException e) {
            logger.error(e.getMessage());
        }
    }

    private void resetTasks() {
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<ProductFetchTask> productFetchTaskQuery = em.createQuery("SELECT t FROM ProductFetchTask t " +
                    "where t.status IN :status " +
                    "ORDER BY t.fetchDate ASC", ProductFetchTask.class);
            productFetchTaskQuery.setParameter("status",
                    ListUtil.toList(new STATUS[]{
                            STATUS.fetching}));
            List<ProductFetchTask> productFetchTasks = productFetchTaskQuery.getResultList();
            for(ProductFetchTask productFetchTask : productFetchTasks) {
                switch (productFetchTask.getStatus()) {
                    case fetching:
                        productFetchTask.setStatus(STATUS.downloadAvailable);
                        break;
                    case planetDownloading:
                        productFetchTask.setStatus(STATUS.planetWaiting);
                        break;
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private void startImageAlerts() {
        imageAlertTimer = new Timer();
        // check every 10 minutes
        long monitoringDelay = 10 * ServerUtil.minuteInMs;
        imageAlertTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Settings settings = Utils.getSettings();
                long monitoringRefresh = Math.max(1, settings.getMonitoringRefresh()) * ServerUtil.hourInMs;
                long timeSlider = Math.max(10, 30) * ServerUtil.dayInMs;
                // get all monitoring tasks which next fetch time is before now
                EntityManager em = EMF.get().createEntityManager();
                try {
                    Date now = new Date();
                    Date lastQueried = new Date(now.getTime() - monitoringRefresh);
                    // get alerts which haven't been queried since the lastQueried date
                    TypedQuery<ImageAlert> query = em.createQuery("SELECT a FROM ImageAlert a WHERE (a.lastQueried < :lastQueried OR a.lastQueried IS NULL) AND a.endDate > :endDate", ImageAlert.class);
                    query.setParameter("lastQueried", lastQueried);
                    query.setParameter("endDate", now);
                    logger.debug("Automatic alert queries to run: " + query.getResultList().size());
                    for (final ImageAlert imageAlert : query.getResultList()) {
                        try {
                            em.getTransaction().begin();
                            // special case for first time image alerts
                            boolean newAlert = imageAlert.getLastQueried() == null;
                            User user = imageAlert.getOwner();
                            final String searchId = imageAlert.getSearchId();
                            SearchRequest search = EIAPIUtil.loadSearchRequest(searchId);
                            search.setStart(new Date((long) Math.max(imageAlert.getCreationTime().getTime(), new Date().getTime() - timeSlider)));
                            search.setStop(new Date());
                            List<Product> products = EIAPIUtil.queryProducts(search).getProducts();
                            imageAlert.setLastQueried(new Date());
                            int totalProducts = products.size();
                            // update the last queried field
                            imageAlert.setLastQueried(now);
                            // we have results
                            if (totalProducts > 0) {
                                // first make sure the products are arranged by ascending date
                                Collections.sort(products, new Comparator<Product>() {
                                    @Override
                                    public int compare(Product firstProduct, Product secondProduct) {
                                        return firstProduct.getStart().compareTo(secondProduct.getStart());
                                    }
                                });
                                // get the previous product ids
                                final String previousProductIds = imageAlert.getPreviousProductIds();
                                // update the automatic alert product ids with the new version
                                imageAlert.setPreviousProductIds(ListUtil.toString(products, new ListUtil.GetLabel<Product>() {
                                    @Override
                                    public String getLabel(Product value) {
                                        return value.getProductId() + ImageAlert.productIdSeparator;
                                    }
                                }, ""));
                                // if not a new alert, we need to check if there are new products and if an email needs to be sent
                                if (!newAlert) {
                                    // if we had values previously make sure we don't count them again
                                    if (previousProductIds != null && previousProductIds.length() != 0) {
                                        // check which ones are new
                                        products = ListUtil.filterValues(products, new ListUtil.CheckValue<Product>() {
                                            @Override
                                            public boolean isValue(Product value) {
                                                // return the products which are not found in the previous list of ids
                                                return previousProductIds.indexOf(value.getProductId() + ImageAlert.productIdSeparator) == -1;
                                            }
                                        });
                                    } else {
                                        // there was no previous products found so there is no need to filter
                                    }
                                    // if there are new results send an email to the user
                                    totalProducts = products.size();
                                    if (totalProducts > 0) {
                                        final String productUrl = settings.getWebsiteUrl() + "/#products:";
                                        MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.CONSUMER);
                                        mailContent.addTitle("Found " + totalProducts + " new products for your alert <b>'" + imageAlert.getName() + "'</b>");
                                        mailContent.addTable(ListUtil.mutate(products.subList(0, Math.min(products.size(), 20)), new ListUtil.Mutate<Product, List<String>>() {
                                            @Override
                                            public List<String> mutate(Product product) {
                                                String thumbnailURL = product.getThumbnail();
                                                return ListUtil.toList(new String[]{
                                                        "<b>" + product.getSatelliteName() + " " + product.getInstrumentName() + "</b>",
                                                        (product.getStart() != null ? "on " + timeFormat.format(product.getStart()) : "Unknown"),
                                                        "<img style='max-width:100px; max-height:100px;' src='" + org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(thumbnailURL) + "'/>",
                                                        "<a style='color: #338; text-decoration: underline;' " +
                                                                "href='" + productUrl +
                                                                com.metaaps.webapps.libraries.client.widget.util.Utils.generateTokens(
                                                                        "searchId", searchId, "productIds", product.getProductId()) +
                                                                "'>View</a>"
                                                });
                                            }
                                        }));
                                        mailContent.addLine("Important! If you wish to unregister your image alert, please click on this link <a href='" +
                                                UtilImageAlert.generateUnregisterLink(imageAlert.getId(), imageAlert.getOwner().getEmail()) + "'>unregister alert</a>.");
                                        try {
                                            mailContent.sendEmail(imageAlert.getOwner(), "New Images for your alert '" + imageAlert.getName() + "' (" + totalProducts + ")");
                                        } catch (EIException e) {
                                            logger.error("Unable to send email for image alerts, reason is " + e.getMessage());
                                        }
                                        EventOrder eventOrder = imageAlert.getEventOrder();
                                        if(eventOrder != null) {
                                            AOIPolygon aoi = new AOIPolygon();
                                            aoi.setName("Area of interest");
                                            ((AOIPolygon) aoi).setPoints(ListUtil.toList(EOLatLng.parseWKT(search.getAoiWKT().replace("POLYGON((", "").replace("))", ""))));
                                            aoi.setId(null);
                                            em.persist(aoi);
                                            // add free products to next batch of processing
                                            addProductsToOrder(em, eventOrder, imageAlert.getSearchId(), "Alert " + imageAlert.getName(), ListUtil.filterValues(products, new ListUtil.CheckValue<Product>() {
                                                @Override
                                                public boolean isValue(Product value) {
                                                    try {
                                                        return PublishAPIUtils.isLocallyFetched(value.getSatelliteName());
                                                    } catch (Exception e) {
                                                        return false;
                                                    }
                                                }
                                            }), aoi);
                                        }
                                    }
                                }
                            }
                            em.getTransaction().commit();
                        } catch (Exception e) {
                            logger.error("Error running image alert service. Reason is " + e.getMessage());
                            if(em.getTransaction().isActive()) {
                                em.getTransaction().rollback();
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                } finally {
                    em.close();
                }
            }
        }, 1000, monitoringDelay);
    }

    private void addProductsToOrder(EntityManager em, EventOrder eventOrder, String searchId, String imageAlertName, List<Product> products, AOI aoi) {
        WKTReader wktReader = new WKTReader();
        WKTWriter wktWriter = new WKTWriter();
        List<ProductOrder> orderedProducts = new ArrayList<ProductOrder>();
        for(Product product : products) {
            ProductRequest productRequest = OrderHelper.createProductRequest(searchId, aoi, product);
            // set selection geometry
            String aoiWKT = AOIUtils.toWKT(aoi);
            // default to aoi wkt
            String selectionWKT = aoiWKT;
            try {
                Geometry intersection = wktReader.read(product.getCoordinatesWKT()).intersection(wktReader.read(aoiWKT));
                selectionWKT = wktWriter.write(intersection);
            } catch (Exception e) {
                logger.error("Error calculating intersection for product " + product.getProductId() + ", reason is: " + e.getMessage(), e);
            }
            em.persist(productRequest);
            ProductOrder productOrder = OrderHelper.createProductOrder(productRequest);
            productOrder.setSelectionGeometry(selectionWKT);
            productOrder.setEventOrder(eventOrder);
            productOrder.setThumbnailURL(product.getThumbnail());
            productOrder.setLabel(imageAlertName);
            em.persist(productOrder);
            orderedProducts.add(productOrder);
            eventOrder.getProductsOrdered().add(productOrder);
        }
        // create the fetch tasks associated
        for(ProductOrder productOrder : orderedProducts) {
            ProductFetchTask productFetchTask = PublishAPIUtils.createProductFetchTask(productOrder);
            em.persist(productFetchTask);
        }
    }

    private void startReportingTimer() {
        // report every hour
        final long reportingPeriod = 60 * ServerUtil.minuteInMs;
        reportingTimer = new Timer();
        reportingTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                {
                    EntityManager em = EMF.get().createEntityManager();
                    try {
                        TypedQuery<Reporting> query = em.createQuery("select r from Reporting r where r.date > :date", Reporting.class);
                        query.setParameter("date", new Date(new Date().getTime() - reportingPeriod));
                        List<Reporting> reportings = query.getResultList();
                        if (reportings != null && reportings.size() > 0) {
                            HashMap<String, List<Reporting>> reportingsMap = ListUtil.createLabelMap(reportings, new ListUtil.AddKeyPair<Reporting, List<Reporting>>() {

                                @Override
                                public String getLabel(Reporting value) {
                                    return value.getType() == null ? "Unknown" : value.getType().toString();
                                }

                                @Override
                                public List<Reporting> getValue(Reporting value, List<Reporting> previousValue) {
                                    if (previousValue == null) {
                                        previousValue = new ArrayList<Reporting>();
                                    }
                                    previousValue.add(value);
                                    return previousValue;
                                }
                            });
                            // check for errors to report
                            MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.ADMIN);
                            for (Reporting.TYPE type : new Reporting.TYPE[]{Reporting.TYPE.ERROR}) {
                                mailContent.addTitle("Reporting on " + type.toString());
                                if (reportingsMap.containsKey(type.toString())) {
                                    for (Reporting reporting : reportingsMap.get(type.toString())) {
                                        mailContent.addLine("<b>" + reporting.getTitle() + " at " + reporting.getDate() + "</b>");
                                        mailContent.addLine(reporting.getContent());
                                    }
                                } else {
                                    mailContent.addLine("No reporting...");
                                }
                            }
                            mailContent.sendEmail(ServerUtil.getUsersAdministrator(), "Administration reporting - " + Utils.getSettings().getServerType());
                        }
                    } catch (Exception e) {
                        logger.error("Could not send administration reporting", e);
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                    } finally {
                        em.close();
                    }
                }
            }
        }, 1000, reportingPeriod);
    }

    private void applyFixes() throws Exception {
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            // make sure settings are set
            Settings settings = Utils.getSettings();
            if (settings == null) {
                Configuration.loadConfiguration();
                settings = new Settings();

                settings.setServerType(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.serverType));
                settings.setEmailFrom(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.emailFrom));
                settings.setEmailServer(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.emailHost));
                settings.setEmailAccount(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.emailAccount));
                settings.setEmailPassword(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.emailPassword));

                settings.setWebsiteUrl(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.websiteUrl));
                settings.setSupportEmail(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.supportEmail));

                em.persist(settings);

            }
            // check for admin users
            {
                TypedQuery<User> usersQuery = em.createQuery("select u from User u where u.userRole = :userRole", User.class);
                usersQuery.setParameter("userRole", USER_ROLE.ADMINISTRATOR);
                List<User> users = usersQuery.getResultList();
                if (users == null || users.size() == 0) {
                    User adminUser = new User();
                    adminUser.setUsername("admin");
                    adminUser.setEmail(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.adminEmail));
                    adminUser.setPasswordHash(ServerUtil.generatePasswordHash("sedas"));
                    adminUser.setRegisteredDate(new Date());
                    // create default settings
                    UserSettings userSettings = new UserSettings();
                    userSettings.setOwner(adminUser);
                    userSettings.setOverlaysOpacity(0.9);
                    userSettings.setProductsOpacity(0.15);
                    userSettings.setAoiOpacity(0.3);
                    adminUser.setSettings(userSettings);
                    // set user role and status
                    adminUser.setUserStatus(USER_STATUS.APPROVED);
                    adminUser.setUserRole(USER_ROLE.ADMINISTRATOR);
                    // create user cart
                    UserCart userCart = new UserCart();
                    userCart.setOwner(adminUser);
                    adminUser.setUserCart(userCart);
                    UserLayers userLayer = new UserLayers();
                    userLayer.setOwner(adminUser);
                    adminUser.setUserLayer(userLayer);
                    em.persist(adminUser);
                }
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private void startTasksService() {
        tasksTimer = new Timer();
        final long monitoringDelay = 10 * ServerUtil.secondInMs;
        tasksTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                runTask();
            }

            private void runTask() {
                Date startTime = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startTime);
                EntityManager em = EMF.get().createEntityManager();
                try {
                    // fetch tasks that have not started yet and need starting
                    TypedQuery<Long> productFetchTaskQuery = em.createQuery("SELECT t.id FROM ProductFetchTask t " +
                            "where t.fetchDate < :date and t.status IN :status " +
                            "ORDER BY t.fetchDate ASC", Long.class);
                    productFetchTaskQuery.setParameter("date", new Date());
                    productFetchTaskQuery.setParameter("status",
                            ListUtil.toList(new STATUS[]{
                                    STATUS.created,
                                    STATUS.planetCreated,
                                    STATUS.planetWaiting,
                                    //STATUS.requested,
                                    STATUS.downloading,
                                    STATUS.downloadAvailable,
                                    STATUS.downloaded,
                                    STATUS.publishing,
                                    STATUS.publishingSuccess}));
                    List<Long> productFetchTasks = productFetchTaskQuery.getResultList();
                    if(productFetchTasks.size() > 0) {
                        logger.debug("Processing " + productFetchTasks.size() + " tasks");
                    } else {
                        logger.debug(".");
                    }
                    int planetRequests = 0;
                    for (Long taskId : productFetchTasks) {
                        try {
                            // task to run once fetch task is updated
                            Runnable productTask = null;
                            em.getTransaction().begin();
                            ProductFetchTask productFetchTask = em.find(ProductFetchTask.class, taskId);
                            ProductOrder productOrder = productFetchTask.getProductOrder();
                            logger.debug("Processing task " + productFetchTask.getId() + " with status " + productFetchTask.getStatus());
                            boolean statusHasChanged = false;
                            switch (productFetchTask.getStatus()) {
                                case created: {
                                    // TODO - do we need to cancel the request if the commit failed???
                                    boolean freeProduct = PublishAPIUtils.isLocallyFetched(productOrder.getProductRequest().getSatelliteName());
                                    try {
                                        if(freeProduct) {
                                            DownloadProductRequest downloadProductRequest = PublishAPIUtils.getDownloadProductRequest(productOrder);
                                            downloadProductRequest.setCallbackUrl(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.callBackUrlProductDownloader));
                                            DownloadProductRequestResponse response = ProductDownloaderAPIUtil.createTask(downloadProductRequest);
                                            productFetchTask.setDownloadTaskId(response.getTaskId());
                                            if (response.getCode() < 300) {
                                                productFetchTask.setStatus(STATUS.downloading);
                                                productOrder.setStatus(PRODUCTORDER_STATUS.InProduction);
                                                statusHasChanged = true;
                                            } else {
                                                // TODO - try again?
                                                productFetchTask.setStatus(STATUS.downloadingFailed);
                                                productFetchTask.setStatusMessage(response.getMessage());
                                                productOrder.setStatus(PRODUCTORDER_STATUS.Failed);
                                                statusHasChanged = true;
                                            }
                                        } else {
                                            // order should have been sent already and status set to requested
                                            // TODO - check status?
                                        }
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                        productFetchTask.setStatusMessage(e.getMessage());
                                    }
                                }
                                break;
                                case planetCreated: {
                                    if(planetRequests++ < 5) {
                                        try {
                                            // use the demo account by default
                                            boolean demoAccount = productOrder.getOwner().getUserRole() == USER_ROLE.ADMINISTRATOR;
                                            // check the image exists in 4 bands first
                                            PlanetAPI.PLATFORM platform = PlanetAPI.getPlatform(productOrder.getProductRequest().getProviderId(), demoAccount);
                                            PlanetAPI.ASSET_TYPE assetType = PlanetAPI.ASSET_TYPE.analytic;
                                            UserOrderParameter userOrderParameter = ListUtil.findValue(productOrder.getParameters(), value -> value.getOrderParameterId().longValue() == 841990);
                                            if(userOrderParameter != null) {
                                                assetType = userOrderParameter.getPropertyValue().toLowerCase().startsWith("analy") ? PlanetAPI.ASSET_TYPE.analytic : PlanetAPI.ASSET_TYPE.visual;
                                            }
                                            // if visual is selected force to 3 bands product
                                            if(assetType == PlanetAPI.ASSET_TYPE.visual) {
                                                platform = PlanetAPI.PLATFORM.PSScene3Band;
                                            }
                                            if(Configuration.getBooleanProperty(Configuration.APPLICATION_SETTINGS.planetOrderV2)) {
                                                String orderId = PlanetAPI.requestOrderV2(platform, assetType, productOrder.getProductRequest().getProviderId(),
                                                        productOrder.getSelectionGeometry(), demoAccount);
                                                productFetchTask.setPlanetOrderId(orderId);
                                            } else {
                                                String downloadUrl = PlanetAPI.requestClipUrl(platform,
                                                        assetType, productOrder.getProductRequest().getProviderId(),
                                                        productOrder.getSelectionGeometry(), demoAccount);
                                                productFetchTask.setPlanetClipUrl(downloadUrl);
                                            }
                                            productFetchTask.setAttempt(0);
                                            productFetchTask.setStatusMessage("");
                                            productFetchTask.setStatus(STATUS.planetWaiting);
                                            productOrder.setStatus(PRODUCTORDER_STATUS.InProduction);
                                            statusHasChanged = true;
                                        } catch (Exception e) {
                                            // try again?
                                            logger.error(e.getMessage(), e);
                                            int attempt = productFetchTask.getAttempt();
                                            if (attempt > 10) {
                                                productFetchTask.setStatus(STATUS.downloadingFailed);
                                                productOrder.setStatus(PRODUCTORDER_STATUS.Failed);
                                                statusHasChanged = true;
                                            } else {
                                                attempt++;
                                                productFetchTask.setAttempt(attempt);
                                                setNextAttemptTask(productFetchTask, attempt);
                                            }
                                            productFetchTask.setStatusMessage(e.getMessage());
                                        }
                                    } else {
                                        logger.debug("Skipping product for throttling requests");
                                    }
                                } break;
                                case planetWaiting: {
                                    // cap the number of requests which can be sent to Planet in one go
                                    if(planetRequests++ < 5) {
                                        try {
                                            // administrators use the demo account by default
                                            boolean demoAccount = productOrder.getOwner().getUserRole() == USER_ROLE.ADMINISTRATOR;
                                            String downloadUrl = null;
                                            if(productFetchTask.getPlanetClipUrl() != null) {
                                                downloadUrl = PlanetAPI.getDownloadClipUrl(productFetchTask.getPlanetClipUrl(), demoAccount);
                                            } else if(productFetchTask.getPlanetOrderId() != null) {
                                                OrderStatus orderStatus = PlanetAPI.getOrderV2DownloadUrl(productFetchTask.getPlanetOrderId(), demoAccount);
                                                if(orderStatus.state != null) {
                                                    switch (orderStatus.state) {
                                                        case "success": {
                                                            if (orderStatus.links.results != null && orderStatus.links.results.size() > 0) {
                                                                // loop through products to find the zip file link for download
                                                                for(OrderStatus.Results linkValue : orderStatus.links.results) {
                                                                    if(linkValue.name.endsWith(".zip")) {
                                                                        downloadUrl = linkValue.location;
                                                                        break;
                                                                    }
                                                                }
                                                                if (downloadUrl == null) {
                                                                    throw new EIException("Product is not available from servers");
                                                                }
                                                            }
                                                        } break;
                                                        case "failed": {
                                                            productFetchTask.setStatus(STATUS.requestFailed);
                                                            productFetchTask.setStatusMessage(orderStatus.lastMessage);
                                                            productOrder.setStatus(PRODUCTORDER_STATUS.Failed);
                                                            statusHasChanged = true;
                                                        } break;
                                                    }
                                                }
                                            } else {
                                                // TODO - send back to planetCreated?
                                                throw new Exception("Problem with task!");
                                            }
                                            // no exception, resets the attempts
                                            productFetchTask.setAttempt(0);
                                            productFetchTask.setStatusMessage("");
                                            if (downloadUrl != null) {
                                                productFetchTask.setStatus(STATUS.planetDownloading);
                                                String finalDownloadUrl = downloadUrl;
                                                productOrder.setStatus(PRODUCTORDER_STATUS.Downloading);
                                                productTask = () -> {
                                                    fetchPlanetFile(productFetchTask.getId(), productOrder.getId(), finalDownloadUrl);
                                                };
                                                statusHasChanged = true;
                                            } else {
                                                // wait some time before next try
                                                setNextAttemptTask(productFetchTask, 1);
                                            }
                                        } catch (Exception e) {
                                            // try again?
                                            logger.error(e.getMessage(), e);
                                            int attempt = productFetchTask.getAttempt();
                                            if (attempt > 10) {
                                                productFetchTask.setStatus(STATUS.downloadingFailed);
                                                productOrder.setStatus(PRODUCTORDER_STATUS.Failed);
                                                statusHasChanged = true;
                                            } else {
                                                attempt++;
                                                productFetchTask.setAttempt(attempt);
                                                setNextAttemptTask(productFetchTask, attempt);
                                            }
                                            productFetchTask.setStatusMessage(e.getMessage());
                                        }
                                    } else {
                                        logger.debug("Skipping product for throttling requests");
                                    }
                                } break;
                                case downloading: {
                                    // check if status has changed
                                    ProductDownloaderTaskDTO productDownloaderTaskDTO = ProductDownloaderAPIUtil.getTaskDTO(productFetchTask.getDownloadTaskId());
                                    switch (productDownloaderTaskDTO.getStatus()) {
                                        case failed:
                                        case failedNotNotified:
                                            productFetchTask.setStatus(STATUS.downloadingFailed);
                                            productFetchTask.setStatusMessage(productDownloaderTaskDTO.getMessage());
                                            productOrder.setStatus(PRODUCTORDER_STATUS.Failed);
                                            statusHasChanged = true;
                                            break;
                                        case downloaded:
                                        case completed:
                                            productFetchTask.setStatus(STATUS.downloadAvailable);
                                            productOrder.setStatus(PRODUCTORDER_STATUS.Downloading);
                                            statusHasChanged = true;
                                            break;
                                        default:
                                            setNextAttemptTask(productFetchTask, 5);
                                    }
                                } break;
                                case downloadAvailable: {
                                    Long productFetchTaskId = productFetchTask.getId();
                                    String productOrderId = productOrder.getId();
                                    productFetchTask.setStatus(STATUS.fetching);
                                    productTask = () -> {
                                        fetchProductFile(productFetchTaskId, productOrderId);
                                    };
                                } break;
                                case downloaded: {
                                    // TODO - move this one to a seperate thread as well, so that it does not block the process
                                        PublishProductRequestResponse response = null;
                                    try {
                                        //TODO - is this OK?
                                        productOrder.setLayerName(productOrder.getId());
                                        PublishProductRequest publishProductRequest = new PublishProductRequest();
                                        String satelliteName = productOrder.getProductRequest().getSatelliteName();
                                        MethodValues.PublishMethod method = PublishAPIUtils.getPublishMethodValue(satelliteName);
                                        if(method == null) {
                                            // no default method for publishing
                                            logger.debug("No default method for publishing for product from satellite " + satelliteName);
                                            productFetchTask.setStatus(STATUS.notPublished);
                                            //productOrder.setStatus(PRODUCTORDER_STATUS.Delivered);
                                            break;
                                        }
                                        publishProductRequest.setProcessId(method.processId);
                                        publishProductRequest.setPlatformName(PublishAPIUtils.getPlatformName(productOrder.getProductRequest().getSatelliteName()));
                                        // TODO - find a way to get the product type
                                        publishProductRequest.setProductType(method.productType);
                                        publishProductRequest.setAoi(productOrder.getSelectionGeometry());
                                        publishProductRequest.setWorkspace(productOrder.getEventOrder().getId());
                                        publishProductRequest.setLayerName(productOrder.getId());
                                        publishProductRequest.setCallbackUrl(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.callBackUrlProductPublisher));
                                        boolean local = Configuration.getBooleanProperty(Configuration.APPLICATION_SETTINGS.localPublisher);
                                        if (local) {
                                            publishProductRequest.setFilePath(productOrder.getFileLocation());
                                            response = ProductPublisherAPIUtil.createTask(publishProductRequest);
                                        } else {
                                            response = ProductPublisherAPIUtil.createTask(new File(productOrder.getFileLocation()), publishProductRequest);
                                        }
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                        setNextAttemptTask(productFetchTask, 5);
                                        productFetchTask.setStatusMessage(e.getMessage());
                                    }
                                    if(response != null) {
                                        productFetchTask.setPublishTaskId(response.getTaskId());
                                        if (response.getCode() < 300) {
                                            productFetchTask.setStatus(STATUS.publishing);
                                            productOrder.setPublicationStatus(PUBLICATION_STATUS.Publishing);
                                        } else {
                                            productFetchTask.setStatus(STATUS.publishingFailed);
                                            productFetchTask.setStatusMessage(response.getMessage());
                                            productOrder.setPublicationStatus(PUBLICATION_STATUS.Failed);
                                        }
                                        statusHasChanged = true;
                                    }
                                } break;
                                // polling in case we missed the message
                                case publishing: {
                                    ProductPublisherTaskDTO productPublisherTaskDTO = null;
                                    try {
                                        productPublisherTaskDTO = ProductPublisherAPIUtil.getProductPublisherTask(productFetchTask.getPublishTaskId());
                                        if(productPublisherTaskDTO != null) {
                                            switch (productPublisherTaskDTO.getStatus()) {
                                                case published: {
                                                    logger.debug("Publishing task has been completed successfully");
                                                    productFetchTask.setStatus(STATUS.publishingSuccess);
                                                    productFetchTask.getProductOrder().setPublicationStatus(PUBLICATION_STATUS.Published);
                                                    productFetchTask.setStatusMessage("Publishing task has been completed successfully");
                                                }
                                                break;
                                                case failed:
                                                case failedNotNotified: {
                                                    logger.debug("Task has failed with message " + productPublisherTaskDTO.getMessage());
                                                    productFetchTask.setStatus(STATUS.publishingFailed);
                                                    productFetchTask.setStatusMessage(productPublisherTaskDTO.getMessage());
                                                    productOrder.setPublicationStatus(PUBLICATION_STATUS.Failed);
                                                }
                                                break;
                                                default:
                                                    setNextAttemptTask(productFetchTask, 5);
                                            }
                                        }
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                        setNextAttemptTask(productFetchTask, 5);
                                        productFetchTask.setStatusMessage(e.getMessage());
                                    }
                                } break;
                                case publishingSuccess: {
                                    try {
                                        // get the stats for the publishing
                                        PublishProcessProducts publishProcessProducts = ProductPublisherAPIUtil.getProductsPublished(productFetchTask.getPublishTaskId());
                                        ProductPublishRequest productPublishRequest = new ProductPublishRequest();
                                        productPublishRequest.setName(publishProcessProducts.getName());
                                        productPublishRequest.setDescription(publishProcessProducts.getDescription());
                                        productPublishRequest.setPublishTaskId(publishProcessProducts.getTaskId());
                                        productPublishRequest.setProductOrder(productOrder);
                                        productPublishRequest.setStatus(PUBLICATION_STATUS.Published);
                                        // the product is published, now make it available to the user
                                        // create product publish request and add to the list
                                        List<ProductPublishRequest> publishRequests = productOrder.getPublishProductRequests();
                                        if (publishRequests == null) {
                                            publishRequests = new ArrayList<ProductPublishRequest>();
                                            productOrder.setPublishProductRequests(publishRequests);
                                        }
                                        publishRequests.add(productPublishRequest);
                                        // generate thumbnail
                                        productOrder.setThumbnailURL(PublisherUtils.generateThumbnail(productOrder));
                                        productOrder.setPublicationStatus(PUBLICATION_STATUS.Published);
                                        // update the product boundaries
                                        productOrder.setSelectionGeometry(publishProcessProducts.getGeneratedProductMetadas().get(0).getCoordinatesWKT());
                                        productFetchTask.setStatus(STATUS.published);
                                        productFetchTask.setCompleted(new Date());
                                        statusHasChanged = true;
                                        logger.info("Finished product publishing task with id " + productFetchTask.getId());
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                        setNextAttemptTask(productFetchTask, 5);
                                        productFetchTask.setStatusMessage(e.getMessage());
                                    }
                                } break;
                            }
                            // check if order status needs update
                            OrderHelper.updateOrderStatus(productOrder.getEventOrder());
                            em.getTransaction().commit();
                            // now run the task if any
                            if(productTask != null) {
                                Future task = executor.submit(productTask);
                            }
                            if(statusHasChanged) {
                                try {
                                    NotificationSocket.notifyProductOrderStatusChanged(productOrder);
                                } catch (Exception e) {
                                    logger.error("Failed to send notification " + e.getMessage(), e);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Failed to process task " + taskId + " reason is " + e.getMessage(), e);
                            if(em.getTransaction().isActive()) {
                                em.getTransaction().rollback();
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error running the product fetch task service. Reason is " + e.getMessage());
                } finally {
                    em.close();
                }
                long updateDelay = Math.max(1000, monitoringDelay - (new Date().getTime() - startTime.getTime()));
                logger.debug("Ran all product fetch tasks, restarting timer with time " + updateDelay);
                tasksTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runTask();
                    }
                }, updateDelay);
            }
        }, 1000);
    }

    private void setNextAttemptTask(ProductFetchTask productFetchTask, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, minutes);
        productFetchTask.setFetchDate(calendar.getTime());
    }

    private void fetchProductFile(Long fetchTaskId, String productOrderId) {
        EntityManager em = EMF.get().createEntityManager();
        try {
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            ProductFetchTask productFetchTask = em.find(ProductFetchTask.class, fetchTaskId);
            try {
                // copy the file locally
                File productDirectory = OrderHelper.getProductDirectory(productOrder, true);
                File downloadedFile = null;
                if(productFetchTask.getDownloadTaskId() != null) {
/*
                    // depending on type of product the URL will be fetched differently
                    ProductDownloadStatsDTO productDownloadStatsDTO = ProductDownloaderAPIUtil.getDownloadStats(productFetchTask.getDownloadTaskId());
                    String url = productDownloadStatsDTO.getDownloadUrl();
                    logger.info("Fetching file product at URL " + url);
                    downloadedFile = UrlUtils.downloadProductFromHTTP(url, productDirectory.getAbsolutePath(), productOrderId + ".zip");
*/
                    downloadedFile = ProductDownloaderAPIUtil.fetchFile(productFetchTask.getDownloadTaskId(), productDirectory);
                } else {
                    if(productFetchTask.getEIProductOrderId() != null) {
                        downloadedFile = EIAPIUtil.fetchFile(productFetchTask.getEIOrderId(), productFetchTask.getEIProductOrderId(), productDirectory);
                    }
                }
                if(downloadedFile == null) {
                    throw new Exception("Could not retrieve file for product fetch task " + productFetchTask.getId());
                }
                em.getTransaction().begin();
                productOrder.setFileLocation(downloadedFile.getAbsolutePath());
                productOrder.setFileSize(downloadedFile.length());
                productOrder.setDeliveredTime(new Date());
                productFetchTask.setStatus(STATUS.downloaded);
                productOrder.setStatus(PRODUCTORDER_STATUS.Completed);
                productOrder.setPublicationStatus(PUBLICATION_STATUS.Requested);
                em.getTransaction().commit();
                try {
                    NotificationSocket.notifyProductOrderStatusChanged(productOrder);
                } catch (Exception e) {
                    logger.error("Failed to send notification " + e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                // problem fetching file, try again another time
                productFetchTask.setStatus(STATUS.downloadAvailable);
                productFetchTask.setStatusMessage(e.getMessage());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
    }

    private void fetchPlanetFile(Long fetchTaskId, String productOrderId, String downloadUrl) {
        EntityManager em = EMF.get().createEntityManager();
        try {
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            ProductFetchTask productFetchTask = em.find(ProductFetchTask.class, fetchTaskId);
            em.getTransaction().begin();
            try {
                // administrators use the demo account by default
                boolean demoAccount = productOrder.getOwner().getUserRole() == USER_ROLE.ADMINISTRATOR;
                // copy the file locally
                File productDirectory = OrderHelper.getProductDirectory(productOrder, true);
                boolean isOrderV2 = Configuration.getBooleanProperty(Configuration.APPLICATION_SETTINGS.planetOrderV2);
                // TODO - make sure product order name is unique
                File downloadedFile = UrlUtils.downloadProductFromHTTP(downloadUrl, productDirectory.getAbsolutePath(), productOrder.getId(), isOrderV2 ? PlanetAPI.getAPIToken(demoAccount) : null);
                if(!downloadedFile.exists()) {
                    throw new Exception("Could not retrieve file for product fetch task " + productFetchTask.getId());
                }
                productOrder.setFileLocation(downloadedFile.getAbsolutePath());
                productOrder.setDeliveredTime(new Date());
                productOrder.setFileSize(downloadedFile.length());
                productFetchTask.setStatus(STATUS.downloaded);
                productOrder.setStatus(PRODUCTORDER_STATUS.Completed);
                productOrder.setPublicationStatus(PUBLICATION_STATUS.Requested);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                // problem fetching file, try again another time
                productFetchTask.setStatus(STATUS.planetWaiting);
                productFetchTask.setStatusMessage(e.getMessage());
                productOrder.setStatus(PRODUCTORDER_STATUS.InProduction);
            }
            em.getTransaction().commit();
            try {
                NotificationSocket.notifyProductOrderStatusChanged(productOrder);
            } catch (Exception e) {
                logger.error("Failed to send notification " + e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            em.close();
        }
    }


    protected void startRateTable() {
        // set rate table as an attribute
        RateTable rateTable = new RateTable();

        // configure the rate table
        rateTable.setBaseRate("USD");
        for(String currency : Price.supportedCurrencies) {
            rateTable.setCurrencyRate(currency, new Double(1));
        }
        loadRateTable(rateTable);
        // update every 6 hours
        rateTableTimer = new Timer();
        long loadRatesPeriodMs = Long.parseLong(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.openexchangeapiperiodhours)) * 3600 * 1000;
        rateTableTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                loadRateTable(rateTable);
            }
        }, loadRatesPeriodMs, loadRatesPeriodMs);

        ServerUtil.setCurrencyRateTable(rateTable);
    }

    private void loadRateTable(RateTable rateTable) {
        logger.trace("Loading table...");
        Measurer measurer = new Measurer(logger);
        measurer.startMeasuring();
        try {
            // First set the default cookie manager.
//            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            URLConnection urlConnection = new URL(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.openexchangeapiurl)).openConnection();
            StringWriter writer = new StringWriter();
            IOUtils.copy(urlConnection.getInputStream(), writer);
            String values = writer.toString(); //ServerUtil.getUrlData("https://finance.yahoo.com/webservice/v1/symbols/allcurrencies/quote?format=json");
            JSONObject currencies = new JSONObject(values).getJSONObject("rates");
            synchronized (rateTable) {
                for(String currency : rateTable.getSupportedCurrencies()) {
                    rateTable.setCurrencyRate(currency, currencies.getDouble(currency));
                }
            }
            measurer.stopMeasuring("Rate table loaded in ");
        } catch (Exception e) {
            logger.error("Failed loading table, reason is " + e.getMessage(), e);
            ServerUtil.addReporting(Reporting.TYPE.ERROR, "Error with rate table loading", "Error with rate table loading, reason is " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
    }

    public void contextDestroyed(ServletContextEvent sce) {
        if(imageAlertTimer != null) {
            imageAlertTimer.cancel();
            imageAlertTimer = null;
        }
        if(reportingTimer != null) {
            reportingTimer.cancel();
            reportingTimer = null;
        }
        if(tasksTimer != null) {
            tasksTimer.cancel();
            tasksTimer = null;
        }
        if(rateTableTimer != null) {
            rateTableTimer.cancel();
            rateTableTimer = null;
        }

        try {
            logger.debug("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            logger.error("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                logger.error("cancel non-finished tasks");
            }
            executor.shutdownNow();
            logger.debug("shutdown finished");
        }

    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    static public Map<String, HttpSession> getSessionMap(ServletContext appContext) {
        Map<String, HttpSession> sessionMap = (Map<String, HttpSession>) appContext.getAttribute("globalSessionMap");
        if (sessionMap == null) {
            sessionMap = new ConcurrentHashMap<String, HttpSession>();
            appContext.setAttribute("globalSessionMap", sessionMap);
        }
        return sessionMap;
    }

    public void sessionCreated(HttpSessionEvent se) {
        Map<String, HttpSession> sessionMap = getSessionMap(se.getSession().getServletContext());
        sessionMap.put(se.getSession().getId(), se.getSession());
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        Map<String, HttpSession> sessionMap = getSessionMap(se.getSession().getServletContext());
        sessionMap.remove(se.getSession().getId());
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
    }

}
