package com.geocento.webapps.earthimages.emis.application.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.earthimages.emis.admin.server.publishapis.ProductDownloaderAPIUtil;
import com.geocento.webapps.earthimages.emis.admin.server.publishapis.ProductPublisherAPIUtil;
import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.common.server.domain.*;
import com.geocento.webapps.earthimages.emis.common.server.domain.Order;
import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.server.mailing.MailContent;
import com.geocento.webapps.earthimages.emis.common.server.utils.*;
import com.geocento.webapps.earthimages.emis.common.server.utils.slack.SlackAPIHelper;
import com.geocento.webapps.earthimages.emis.common.share.*;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.utils.RateTable;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.geocento.webapps.earthimages.emis.application.server.publishapi.MethodValues;
import com.geocento.webapps.earthimages.emis.application.server.publishapi.PublishAPIUtils;
import com.geocento.webapps.earthimages.emis.application.server.utils.PolicyHelper;
import com.geocento.webapps.earthimages.emis.application.server.utils.ProductRequestUtil;
import com.geocento.webapps.earthimages.emis.application.server.websocket.NotificationSocket;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.*;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.metaaps.webapps.earthimages.extapi.server.domain.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.orders.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.*;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.property.domain.*;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.xero.model.*;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CustomerServiceImpl extends ProxyCompatibleRemoteServiceServlet implements CustomerService {

    public static SerializationPolicy serializationPolicy;

    private static KeyGenerator keyGenerator = new KeyGenerator(16);

    public CustomerServiceImpl() {
        // create the logger
        logger = Logger.getLogger(CustomerServiceImpl.class);
        logger.info("Starting customer service");
    }

    @Override
    public ResourcesDTO loadResources() throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        try {
            ResourcesDTO resourcesDTO = new ResourcesDTO();
            resourcesDTO.setSatellites(EIAPIUtil.listSatellites());
            return resourcesDTO;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EIException("Failed to retrieve resources from server");
        }
    }

    @Override
    public SearchRequest loadSearch(String searchId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        try {
            return EIAPIUtil.loadSearchRequest(searchId);
        } catch (Exception e) {
            if(e instanceof EIException) {
                throw (EIException) e;
            }
            logger.error(e.getMessage(), e);
            throw new EIException("Error loading search request");
        }
    }

    @Override
    public SearchResponseDTO storeAndSearchCatalog(SearchRequest searchRequest) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            // check product order exists, that it belongs to the user, that payment has been requested and that the price match
            em.getTransaction().begin();

            // retrieve user first
            User dbUser = em.find(User.class, logUserName);
            // make sure start and stop are in the past
            Date now = new Date();
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            calendar.setTime(now);
            calendar.add(Calendar.DATE, Utils.getSettings().getMaxDaysFuture());
            Date maxTime = calendar.getTime();
            if(searchRequest.getStop().after(maxTime)) {
                searchRequest.setStop(maxTime);
            }
            // avoid same day searches
            calendar.setTime(now);
            int currentDate = calendar.get(Calendar.DATE);
            int currentYear = calendar.get(Calendar.YEAR);
            calendar.setTime(searchRequest.getStop());
            //calendar.add(Calendar.SECOND, -10);
            if(currentYear == calendar.get(Calendar.YEAR) && currentDate == calendar.get(Calendar.DATE)) {
                // set the time to the current time
                searchRequest.setStop(now);
            }
            String currency = dbUser.getCredit() == null ? null : dbUser.getCredit().getCurrency();
            searchRequest.setCurrency(currency);
            searchRequest.setLimit(Utils.getSettings().getQueryLimit());
            logger.debug("Payload sent is " + new ObjectMapper().writeValueAsString(searchRequest));
            logger.debug("Start time timestamp is " + searchRequest.getStart().getTime());
            logger.debug("Stop time timestamp is " + searchRequest.getStop().getTime());
            return getSearchResponse(logUserName, EIAPIUtil.queryProducts(searchRequest));
        } catch (Exception e) {
            if(e instanceof EIException) {
                throw (EIException) e;
            }
            logger.error(e.getMessage(), e);
            throw new EIException("Error querying server");
        }
    }

    @Override
    public SearchResponseDTO searchCatalogue(String searchId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        try {
            return getSearchResponse(logUserName, EIAPIUtil.queryProducts(searchId));
        } catch (Exception e) {
            if(e instanceof EIException) {
                throw (EIException) e;
            }
            logger.error(e.getMessage(), e);
            throw new EIException("Error querying server");
        }
    }

    private SearchResponseDTO getSearchResponse(String userName, SearchResponse searchResponse) {
        SearchResponseDTO searchResponseDTO = new SearchResponseDTO();
        searchResponseDTO.setSearchResponse(searchResponse);
        List<String> productIds = ListUtil.mutate(searchResponseDTO.getSearchResponse().getProducts(), value -> {
            return value.getProductId();
        });
        if(productIds.size() > 0) {
            searchResponseDTO.setProductOrders(ListUtil.mutate(OrderHelper.getOrderedProducts(userName, productIds), value -> {
                return convertProductOrderDTO(value);
            }));
        }
        return searchResponseDTO;
    }

    @Override
    public ApplicationSettingsDTO loadApplicationSettings() {
        Settings settings = Utils.getSettings();
        ApplicationSettingsDTO applicationSettingsDTO = new ApplicationSettingsDTO();
        applicationSettingsDTO.setApplicationName(settings.getApplicationName());
        applicationSettingsDTO.setContactUs(settings.getContactUsURL());
        applicationSettingsDTO.setWmsURL(settings.getWMSLayersUrl());
        applicationSettingsDTO.setContactInfoSales(settings.getContactInfoSales());
        applicationSettingsDTO.setAboutUsURL(settings.getAboutUsURL());
        applicationSettingsDTO.setTermsOfSalesUrl(settings.getTermsAndConditionsURL());
        applicationSettingsDTO.setQueryLimit(settings.getQueryLimit());
        applicationSettingsDTO.setMaxArea(settings.getMaxArea());
        applicationSettingsDTO.setMaxDaysFuture(settings.getMaxDaysFuture());
        return applicationSettingsDTO;
    }

    @Override
    public List<UserLayerDTO> loadUserLayers() throws EIException {

        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();

        try {

            User user = em.find(User.class, logUserName);
            UserLayers userLayer = user.getUserLayer();
            if(userLayer == null) {
                return new ArrayList<UserLayerDTO>();
            }
            return convertUserLayers(userLayer.getLayers());
        } catch (Exception e) {
            logger.debug("Could not load WMS layers reason:" + e.getMessage());
            throw new EIException("Could not load WMS layers");
        }
    }

    @Override
    public List<UserLayerDTO> addUserLayers(List<LayerDTO> layerDTOs) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();

        try {
            em.getTransaction().begin();

            User user = em.find(User.class, logUserName);
            UserLayers userLayers = user.getUserLayer();
            if(userLayers == null) {
                userLayers = new UserLayers();
                userLayers.setOwner(user);
                em.persist(userLayers);
                user.setUserLayer(userLayers);
            }

            // TODO - check for duplicates?
            List<UserLayer> addedLayers = new ArrayList<UserLayer>();
            List<UserLayerDTO> addedLayerDTOs = new ArrayList<UserLayerDTO>();
            for(LayerDTO layerDTO : layerDTOs) {
                // check for duplicates
                if(ListUtil.findValue(userLayers.getLayers(), value -> value.getBaseUrl().contentEquals(layerDTO.getBaseUrl()) && value.getLayerName().contentEquals(layerDTO.getLayerName())) != null) {
                    // skip if there is already a matching layer
                    continue;
                }
                UserLayer userLayer = new UserLayer();
                userLayer.setLayerType(layerDTO.getLayerType());
                userLayer.setBaseUrl(layerDTO.getBaseUrl());
                userLayer.setLayerName(layerDTO.getLayerName());
                userLayer.setName(layerDTO.getName());
                userLayer.setDescription(layerDTO.getDescription());
                userLayer.setVersion(layerDTO.getVersion());
                CoverageDTO coverageDTO = layerDTO.getCoverageDTO();
                if(coverageDTO != null) {
                    userLayer.setWcsUrl(coverageDTO.getBaseUrl());
                    userLayer.setCoverageId(coverageDTO.getCoverageId());
                }
                userLayer.setCreationTime(new Date());
                userLayer.setUserLayers(userLayers);
                addedLayers.add(userLayer);
                em.persist(userLayer);
                UserLayerDTO userLayerDTO = new UserLayerDTO();
                userLayerDTO.setId(userLayer.getId());
                userLayerDTO.setLayerDTO(layerDTO);
                addedLayerDTOs.add(userLayerDTO);
            }
            userLayers.getLayers().addAll(addedLayers);

            em.getTransaction().commit();

            return addedLayerDTOs;
        } catch(Exception e) {
            handleException(em, e);
            throw new EIException("Server error.");
        } finally {
            em.close();
        }
    }

    private List<UserLayerDTO> convertUserLayers(List<UserLayer> layers) {
        return ListUtil.mutate(layers, value -> {
            UserLayerDTO userLayerDTO = new UserLayerDTO();
            userLayerDTO.setId(value.getId());
            userLayerDTO.setLayerDTO(getLayerDTO(value));
            return userLayerDTO;
        });
    }

    private LayerDTO getLayerDTO(UserLayer userLayer) {
        LayerDTO layerDTO = new LayerDTO();
        layerDTO.setLayerType(userLayer.getLayerType());
        layerDTO.setBaseUrl(userLayer.getBaseUrl());
        layerDTO.setLayerName(userLayer.getLayerName());
        // add these as there is an issue with the group layers and geoserver API not storing name and description
        layerDTO.setName(userLayer.getName());
        layerDTO.setDescription(userLayer.getDescription());
        layerDTO.setVersion(userLayer.getVersion());
        // call the servers to get the information
//        WMSCapabilities.callOWSServer(userLayer.getBaseUrl(), "GetCapabilities", null, );
        return layerDTO;
    }

    @Override
    public void deleteUserLayer(Long id) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();

        try {

            User user = em.find(User.class, logUserName);
            UserLayer userLayer = em.find(UserLayer.class, id);
            if(userLayer == null) {
                throw new EIException("Unknown user layer");
            }
            if(userLayer.getUserLayers().getOwner() != user) {
                throw new EIException("Not allowed");
            }

            em.getTransaction().begin();
            user.getUserLayer().removeLayer(userLayer);
            em.remove(userLayer);
            em.getTransaction().commit();

        } catch(Exception e) {
            handleException(em, e);
            throw new EIException("Server error.");
        } finally {
            em.close();
        }
    }

    @Override
    public CartRequestsDTO loadProductRequests() throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            // create the response
            CartRequestsDTO cartRequestsDTO = new CartRequestsDTO();
            List<ProductRequestDTO> productRequestDTOs = new ArrayList<ProductRequestDTO>();
            User user = em.find(User.class, logUserName);
            // start with catalogue products
            List<ProductRequest> productRequests = user.getUserCart().getProductRequests();
            if(productRequests != null && productRequests.size() > 0) {
                // first retrieve all products
                final List<Product> products = EIAPIUtil.queryProductsById(ListUtil.toString(productRequests, new ListUtil.GetLabel<ProductRequest>() {
                    @Override
                    public String getLabel(ProductRequest value) {
                        return value.getGeocentoid();
                    }
                }, ","));
                // now retrieve the ordering policies
                List<Long> instrumentIds = ListUtil.mutate(products, product -> product.getInstrumentId());
                final List<ProductPolicy> policies = EIAPIUtil.getCataloguePoliciesInstrument(instrumentIds);
                productRequestDTOs.addAll(ListUtil.mutate(productRequests, new ListUtil.Mutate<ProductRequest, ProductRequestDTO>() {
                    @Override
                    public ProductRequestDTO mutate(final ProductRequest productRequest) {
                        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
                        productRequestDTO.setId(productRequest.getId());
                        productRequestDTO.setAoI(productRequest.getAoi());
                        final Product product = ListUtil.findValue(products, new ListUtil.CheckValue<Product>() {
                            @Override
                            public boolean isValue(Product value) {
                                return value.getProductId().contentEquals(productRequest.getGeocentoid());
                            }
                        });
                        productRequestDTO.setProduct(product);
                        List<EOLatLng> selectionGeometry = productRequest.getSelectionGeometry();
                        if (selectionGeometry != null) {
                            productRequestDTO.setSelectionGeometry(selectionGeometry.toArray(new EOLatLng[selectionGeometry.size()]));
                        }
                        productRequestDTO.setPolicy(ListUtil.findValue(policies, (ListUtil.CheckValue<ProductPolicy>) value -> value.getInstrumentIds().contains(product.getInstrumentId())));
                        productRequestDTO.setOrderingOptions(new ArrayList<UserOrderParameter>());
                        productRequestDTO.setCreationTime(productRequest.getCreationTime());
                        return productRequestDTO;
                    }
                }));
            }
            // check for already ordered products
            List<String> productIds = ListUtil.mutate(productRequestDTOs, value -> value.getProduct().getProductId());
            if(productIds.size() > 0) {
                cartRequestsDTO.setProductOrders(ListUtil.mutate(OrderHelper.getOrderedProducts(em, logUserName, productIds), value -> convertProductOrderDTO(value)));
            }
            // now add the tasking requests
            List<TaskingRequest> taskingRequests = user.getUserCart().getTaskingRequests();
            if(taskingRequests != null && taskingRequests.size() > 0) {
                List<Long> taskingInstrumentIds = ListUtil.mutate(taskingRequests, product -> product.getInstrumentId());
                final List<ProductPolicy> policies = EIAPIUtil.getTaskingPoliciesInstrument(taskingInstrumentIds);
                productRequestDTOs.addAll(ListUtil.mutate(taskingRequests, new ListUtil.Mutate<TaskingRequest, ProductRequestDTO>() {
                    @Override
                    public ProductRequestDTO mutate(TaskingRequest taskingRequest) {
                        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
                        productRequestDTO.setId(taskingRequest.getId());
                        productRequestDTO.setAoI(taskingRequest.getAoi());
                        // generate product out of fields available
                        Product product = ProductRequestUtil.convertToProduct(taskingRequest.getProductEntity());
                        product.setType(TYPE.TASKING);
                        productRequestDTO.setProduct(product);
                        List<EOLatLng> selectionGeometry = taskingRequest.getSelectionGeometry();
                        if (selectionGeometry != null) {
                            productRequestDTO.setSelectionGeometry(selectionGeometry.toArray(new EOLatLng[selectionGeometry.size()]));
                        }
                        productRequestDTO.setPolicy(ListUtil.findValue(policies, (ListUtil.CheckValue<ProductPolicy>) value -> value.getInstrumentIds().contains(taskingRequest.getInstrumentId())));
                        productRequestDTO.setOrderingOptions(new ArrayList<UserOrderParameter>());
                        productRequestDTO.setCreationTime(taskingRequest.getCreationTime());
                        return productRequestDTO;
                    }
                }));
            }
            cartRequestsDTO.setProductRequestDTOs(productRequestDTOs);
            return cartRequestsDTO;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public String createOrder(String name, String additionalInformation, List<ProductRequestDTO> products) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, logUserName);
            if(!user.isCanOrder()) {
                throw new EIException("You cannot order any products, please contact earthimages@geocento.com for approval");
            }
            UserCart cart = user.getUserCart();
            // update cart first
            updateCart(cart, products);
            // create the order
            Order order = OrderHelper.createOrder(name, additionalInformation);
            // check order name is unique for the user
            TypedQuery<Long> query = em.createQuery("select count(o) from orders o where o.owner = :user and o.name = :name", Long.class);
            query.setParameter("user", user);
            query.setParameter("name", order.getName());
            if(query.getSingleResult() > 0) {
                throw new EIException("The name for this order has already been taken");
            }
            order.setOwner(user);
            user.getOrders().add(order);
            em.persist(order);
            addProductsToOrder(em, order, cart.getProductRequests(), cart.getTaskingRequests(), products, user);
            for(ProductRequest productRequest : cart.getProductRequests()) {
                productRequest.setUserCart(null);
            }
            cart.getProductRequests().clear();
            for(TaskingRequest taskingRequest : cart.getTaskingRequests()) {
                taskingRequest.setUserCart(null);
            }
            cart.getTaskingRequests().clear();
            OrderHelper.updateOrderStatus(order);
            em.getTransaction().commit();
            return order.getId();
        } catch(Exception e) {
            // TODO - cancel the requests???
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private void addProductsToOrder(EntityManager em, Order order, List<ProductRequest> catalogueProductRequests, List<TaskingRequest> taskingRequests, List<ProductRequestDTO> products, User user) throws Exception {
        List<ProductOrder> orderedProducts = new ArrayList<ProductOrder>();
        // add catalogue products
        for(ProductRequest productRequest : catalogueProductRequests) {
            // get the matching product request DTO as it has the product information
            ProductRequestDTO productRequestDTO = ListUtil.findValue(products, value -> value.getId().equals(productRequest.getId()));
            // create the product order
            ProductOrder productOrder = OrderHelper.createProductOrder(productRequest);
            productOrder.setPolicyId(productRequestDTO.getPolicy().getId());
            productOrder.setParameters(productRequestDTO.getOrderingOptions());
            productOrder.setLicenseOption(productRequestDTO.getLicenseOption());
            productOrder.setThumbnailURL(productRequestDTO.getProduct().getThumbnail());
            productOrder.setTotalPrice(productRequestDTO.getTotalPrice());
            productOrder.setCurrency(productRequestDTO.getCurrency());
            productOrder.setOrder(order);
            em.persist(productOrder);
            orderedProducts.add(productOrder);
            order.getProductOrders().add(productOrder);
        }
        // add tasking products
        for(TaskingRequest taskingRequest : taskingRequests) {
            ProductRequestDTO productRequestDTO = ListUtil.findValue(products, value -> value.getId().equals(taskingRequest.getId()));
            // create the product order
            ProductOrder productOrder = OrderHelper.createProductOrder(taskingRequest);
            productOrder.setPolicyId(productRequestDTO.getPolicy().getId());
            productOrder.setParameters(productRequestDTO.getOrderingOptions());
            productOrder.setLicenseOption(productRequestDTO.getLicenseOption());
            productOrder.setOrder(order);
            productOrder.setTotalPrice(productRequestDTO.getTotalPrice());
            productOrder.setCurrency(productRequestDTO.getCurrency());
            em.persist(productOrder);
            orderedProducts.add(productOrder);
            order.getProductOrders().add(productOrder);
        }
        // set the ordering options
        // keep track of all automated purchases
        Price automatedPurchase = null;
        List<ProductOrder> automatedPurchaseProducts = new ArrayList<ProductOrder>();
        // create the fetch tasks associated
        List<ProductOrder> locallyFetchedProducts = new ArrayList<>();
        List<ProductOrder> eiFetchedProducts = new ArrayList<>();
        for(ProductOrder productOrder : orderedProducts) {
            boolean freeProduct = productOrder.getProductRequest() != null &&
                    PublishAPIUtils.isLocallyFetched(productOrder.getProductRequest().getSatelliteName());
            if(freeProduct) {
                locallyFetchedProducts.add(productOrder);
            } else if (isAutomatedOrder(productOrder)) {
                // automated orders so check there is enough credit otherwise set to quoted
                Double offeredPrice = productOrder.getTotalPrice();
/*
                if(user.isChargeVAT()) {
                    offeredPrice = offeredPrice * 1.2;
                }
*/
                productOrder.setOfferedPrice(offeredPrice);
                // first check global license has been signed
                if(ProductRequestUtil.licenseNeedsSigning(user, productOrder.getPolicyId())) {
                    productOrder.setStatus(PRODUCTORDER_STATUS.Documentation);
                } else {
                    productOrder.setStatus(PRODUCTORDER_STATUS.Quoted);
/*
                    Price currentCredit = new Price(user.getCredit().getCurrent(), user.getCredit().getCurrency());
                    // use the converted total price
                    ProductRequestDTO productRequestDTO = ListUtil.findValue(products, value -> value.getId().equals(productOrder.getProductRequest().getId()));
                    Price productPrice = productRequestDTO.getConvertedTotalPrice(); //new Price(productOrder.getTotalPrice(), productOrder.getCurrency());
                    if (currentCredit.getValue() > productPrice.getValue()) {
                        //user.getCredit().setCurrent(user.getCredit().getCurrent() - productPrice.getValue());
                        planetFetchedProducts.add(productOrder);
                        // keep track of transactions
                        if (automatedPurchase == null) {
                            automatedPurchase = new Price(productPrice.getValue(), productPrice.getCurrency());
                        } else {
                            automatedPurchase.add(productPrice);
                        }
                        automatedPurchaseProducts.add(productOrder);
                        // update temporary credit value to make sure it can be ordered
                        currentCredit.setValue(user.getCredit().getCurrent() - productPrice.getValue());
                        // update paid price
                        productOrder.setPaidPrice(productPrice);
                        productOrder.setPaidDate(new Date());
                    } else {
                        productOrder.setStatus(PRODUCTORDER_STATUS.Quoted);
                    }
*/
                }
            } else {
                eiFetchedProducts.add(productOrder);
            }
        }
        // locally fetched free products
        for(ProductOrder productOrder : locallyFetchedProducts) {
            // delay the call to the state machine
            ProductFetchTask productFetchTask = PublishAPIUtils.createProductFetchTask(productOrder);
            productFetchTask.setStatus(STATUS.created);
            em.persist(productFetchTask);
        }
        // EI ordered products
        if(eiFetchedProducts.size() > 0) {
            CreateOrderRequest createOrderRequest = new CreateOrderRequest();
            List<String> previousEIOrders = order.getEIOrderIds();
            if(previousEIOrders == null) {
                previousEIOrders = new ArrayList<>();
                order.setEIOrderIds(previousEIOrders);
            }
            int orderCount = previousEIOrders.size() + 1;
            createOrderRequest.setName(order.getName() + " - batch " + orderCount);
            createOrderRequest.setDescription("Application order from " + new Date().toString() + ". Description provided: " + order.getDescription());
            UserInformation userInformation = new UserInformation();
            userInformation.setName(user.getFirstName() + " " + user.getLastName());
            userInformation.setEmailAddress(user.getEmail());
            userInformation.setCountry(user.getCountryCode());
            userInformation.setApplication(user.getDomain() == null ? null : user.getDomain().toString());
            createOrderRequest.setUserInformation(userInformation);
            // add catalogue product orders
            List<ProductOrder> eiFetchedCatalogueProducts = ListUtil.filterValues(eiFetchedProducts, value -> {
                return value.getProductRequest() != null;
            });
            createOrderRequest.setCatalogueProductOrderRequests(ListUtil.mutate(eiFetchedCatalogueProducts, new ListUtil.Mutate<ProductOrder, CatalogueProductOrderRequest>() {
                @Override
                public CatalogueProductOrderRequest mutate(ProductOrder productOrder) {
                    CatalogueProductOrderRequest catalogueProductOrderRequest = new CatalogueProductOrderRequest();
                    ProductRequest productRequest = productOrder.getProductRequest();
                    catalogueProductOrderRequest.setGeocentoId(productRequest.getGeocentoid());
                    catalogueProductOrderRequest.setProviderId(productRequest.getProviderId());
                    catalogueProductOrderRequest.setSearchId(productRequest.getSearchId());
                    catalogueProductOrderRequest.setAoiWKT(AOIUtils.toWKT(productRequest.getAoi()));
                    AOIPolygon aoiPolygon = new AOIPolygon();
                    aoiPolygon.setPoints(productRequest.getSelectionGeometry());
                    catalogueProductOrderRequest.setSelectionType(PRODUCT_SELECTION.shape);
                    catalogueProductOrderRequest.setGeometrySelectionWKT(AOIUtils.toWKT(aoiPolygon));
                    catalogueProductOrderRequest.setOrderParameters(new ArrayList<OrderParameterRequest>());
                    catalogueProductOrderRequest.setOrderParameters(ListUtil.mutate(productOrder.getParameters(), userOrderParameter -> convertUserOrderParameter(userOrderParameter)));
                    catalogueProductOrderRequest.setLicense(convertUserOrderParameter(productOrder.getLicenseOption()));
                    return catalogueProductOrderRequest;
                }
            }));
            // add tasking product orders
            List<ProductOrder> eiFetchedTaskingProducts = ListUtil.filterValues(eiFetchedProducts, value -> {
                return value.getTaskingRequest() != null;
            });
            createOrderRequest.setTaskingProductOrderRequests(ListUtil.mutate(eiFetchedTaskingProducts, new ListUtil.Mutate<ProductOrder, TaskingProductOrderRequest>() {
                @Override
                public TaskingProductOrderRequest mutate(ProductOrder productOrder) {
                    TaskingProductOrderRequest taskingProductOrderRequest = new TaskingProductOrderRequest();
                    TaskingRequest taskingRequest = productOrder.getTaskingRequest();
                    taskingProductOrderRequest.setProductId(taskingRequest.getProductId());
                    taskingProductOrderRequest.setAoiWKT(AOIUtils.toWKT(taskingRequest.getAoi()));
                    AOIPolygon aoiPolygon = new AOIPolygon();
                    aoiPolygon.setPoints(taskingRequest.getSelectionGeometry());
                    taskingProductOrderRequest.setSelectionType(PRODUCT_SELECTION.shape);
                    taskingProductOrderRequest.setGeometrySelectionWKT(AOIUtils.toWKT(aoiPolygon));
                    ProductEntity productEntity = taskingRequest.getProductEntity();
                    taskingProductOrderRequest.setAscendingNodeDate(productEntity.getAscendingNodeDate());
                    taskingProductOrderRequest.setCompletionTimeFromAscendingNodeDate(productEntity.getCompletionTimeFromAscendingNodeDate());
                    taskingProductOrderRequest.setCoordinatesWKT(productEntity.getCoordinates());
                    taskingProductOrderRequest.setModeId(productEntity.getModeId());
                    taskingProductOrderRequest.setOrbit(productEntity.getOrbit());
                    taskingProductOrderRequest.setOrbitDirection(productEntity.getOrbitDirection());
                    taskingProductOrderRequest.setSearchId(taskingRequest.getSearchId());
                    taskingProductOrderRequest.setOrderPolicyId(taskingRequest.getPolicyId());
                    taskingProductOrderRequest.setStart(productEntity.getStart());
                    taskingProductOrderRequest.setStop(productEntity.getStop());
                    taskingProductOrderRequest.setOrderPolicyId(taskingRequest.getPolicyId());
                    taskingProductOrderRequest.setOrderParameters(ListUtil.mutate(productOrder.getParameters(), userOrderParameter -> convertUserOrderParameter(userOrderParameter)));
                    taskingProductOrderRequest.setLicense(convertUserOrderParameter(productOrder.getLicenseOption()));
                    return taskingProductOrderRequest;
                }
            }));
            createOrderRequest.setCallbackUrl(Utils.getSettings().getEIProductCallbackURL());
            CreateOrderResponse createOrderResponse = null;
            try {
                createOrderResponse = EIAPIUtil.createOrderRequest(createOrderRequest);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
            String eiOrderId = createOrderResponse.getId();
            previousEIOrders.add(eiOrderId);
            // get the order and match the product order ids
            com.metaaps.webapps.earthimages.extapi.server.domain.orders.Order eiOrder = EIAPIUtil.getOrder(eiOrderId);
            // set the ei product order id
            for(ProductOrder productOrder : eiFetchedProducts) {
                Long eiProductOrderId = null;
                boolean isCatalogue = productOrder.getProductRequest() != null;
                if(isCatalogue) {
                    CatalogueProductOrder eiProduct = ListUtil.findValue(eiOrder.getCatalogueProductOrders(), (ListUtil.CheckValue<CatalogueProductOrder>) value -> value.getGeocentoId().contentEquals(productOrder.getProductRequest().getGeocentoid()));
                    if(eiProduct != null) {
                        eiProductOrderId = eiProduct.getId();
                    }
                } else {
                    TaskingProductOrder eiProduct = ListUtil.findValue(eiOrder.getTaskingProductOrders(), (ListUtil.CheckValue<TaskingProductOrder>) value -> value.getProductId().contentEquals(productOrder.getTaskingRequest().getProductId()));
                    if(eiProduct != null) {
                        eiProductOrderId = eiProduct.getId();
                    }
                }
                // TODO - call the EI API to remove the order?
                if(eiProductOrderId == null) {
                    throw new Exception("Could not find matching product order for product order id " + productOrder.getId());
                }
                ProductFetchTask productFetchTask = PublishAPIUtils.createProductFetchTask(productOrder);
                productFetchTask.setEIOrderId(eiOrderId);
                productFetchTask.setEIProductOrderId(eiProductOrderId);
                // update statuses
                productFetchTask.setStatus(STATUS.requested);
                productOrder.setStatus(PRODUCTORDER_STATUS.Submitted);
                em.persist(productFetchTask);
            }
        }
        // update and send notification only if credit has changed
        if (automatedPurchase != null) {
            String productsName = ListUtil.toString(automatedPurchaseProducts, value -> {return "#" + value.getId();}, ", ");
            Transaction transaction = TransactionsHelper.addUserTransaction(em, user, TRANSACTION_TYPE.purchase, (-1) * automatedPurchase.getValue(), automatedPurchase.getCurrency(), "Automated purchase of product orders " + productsName, new Date());
            transaction.setOrder(order);
            NotificationSocket.notifyCreditsUpdated(user);
        }
        // update the last updated date
        order.setLastUpdate(new Date());
    }

    private OrderParameterRequest convertUserOrderParameter(UserOrderParameter userOrderParameter) {
        OrderParameterRequest orderParameterRequest = new OrderParameterRequest();
        orderParameterRequest.setOrderParameterId(userOrderParameter.getOrderParameterId());
        orderParameterRequest.setValue(userOrderParameter.getPropertyValue());
        return orderParameterRequest;
    }

    @Override
    public String addProductsToOrder(String orderId, List<ProductRequestDTO> products) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, logUserName);
            if(!user.isCanOrder()) {
                throw new EIException("You cannot order any products, please contact earthimages@geocento.com for approval");
            }
            UserCart cart = user.getUserCart();
            // update cart first
            updateCart(cart, products);
            // create the order
            Order order = em.find(Order.class, orderId);
            if (order == null) {
                throw new EIException("Could not find order with id " + orderId);
            }
            addProductsToOrder(em, order, cart.getProductRequests(), cart.getTaskingRequests(), products, user);
            for(ProductRequest productRequest : cart.getProductRequests()) {
                productRequest.setUserCart(null);
            }
            cart.getProductRequests().clear();
            for(TaskingRequest taskingRequest : cart.getTaskingRequests()) {
                taskingRequest.setUserCart(null);
            }
            cart.getTaskingRequests().clear();
            OrderHelper.updateOrderStatus(order);
            em.getTransaction().commit();
            return order.getId();
        } catch(Exception e) {
            // TODO - cancel the requests???
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    // update cart based on latest product requests
    private void updateCart(UserCart cart, List<ProductRequestDTO> productRequestDTOs) throws EIException {
        List<ProductRequestDTO> catalogueRequests = ListUtil.filterValues(productRequestDTOs, value -> {return value.getProduct().getType() == TYPE.ARCHIVE;});
        List<ProductRequestDTO> taskingRequests = ListUtil.filterValues(productRequestDTOs, value -> {return value.getProduct().getType() == TYPE.TASKING;});
        for(ProductRequestDTO productRequestDTO : catalogueRequests) {
            ProductRequest productRequest = ListUtil.findValue(cart.getProductRequests(), new ListUtil.CheckValue<ProductRequest>() {
                @Override
                public boolean isValue(ProductRequest value) {
                    return value.getId().equals(productRequestDTO.getId());
                }
            });
            if (productRequest == null) {
                throw new EIException("Product request does not exist!");
            }
            // update the values
            productRequest.setSelectionGeometry(ListUtil.toList(productRequestDTO.getSelectionGeometry()));
            productRequest.setSelectionType(SELECTION.valueOf(productRequestDTO.getSelectionType().toString()));
        }
        for(ProductRequestDTO productRequestDTO : taskingRequests) {
            TaskingRequest taskingRequest = ListUtil.findValue(cart.getTaskingRequests(), new ListUtil.CheckValue<TaskingRequest>() {
                @Override
                public boolean isValue(TaskingRequest value) {
                    return value.getId().equals(productRequestDTO.getId());
                }
            });
            if (taskingRequest == null) {
                throw new EIException("Product request does not exist!");
            }
            // update the values
            taskingRequest.setSelectionGeometry(ListUtil.toList(productRequestDTO.getSelectionGeometry()));
            taskingRequest.setSelectionType(SELECTION.valueOf(productRequestDTO.getSelectionType().toString()));
        }
        // now check if some products have been removed
        if(catalogueRequests.size() != cart.getProductRequests().size()) {
            for(ProductRequest productRequest : cart.getProductRequests()) {
                if(ListUtil.findValue(catalogueRequests, value -> value.getId().equals(productRequest.getId())) == null) {
                    productRequest.setUserCart(null);
                    cart.getProductRequests().remove(productRequest);
                }
            }
        }
        if(taskingRequests.size() != cart.getTaskingRequests().size()) {
            for(TaskingRequest taskingRequest : cart.getTaskingRequests()) {
                if(ListUtil.findValue(taskingRequests, value -> value.getId().equals(taskingRequest.getId())) == null) {
                    taskingRequest.setUserCart(null);
                    cart.getTaskingRequests().remove(taskingRequest);
                }
            }
        }
    }

    @Override
    public PaymentTransactionDTO acceptAndMakeProductOrdersPayment(String orderId, HashMap<String, Price> productsPrices) throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            // check product order exists, that it belongs to the user, that payment has been requested and that the price match
            em.getTransaction().begin();

            // retrieve user first
            User dbUser = em.find(User.class, logUserName);
            Set<String> ids = productsPrices.keySet();
            if(ids == null || ids.size() == 0) {
                throw new EIException("Invalid product order");
            }
            // get all products
            TypedQuery<ProductOrder> query = em.createQuery("Select p from ProductOrder  p where p.id in :ids", ProductOrder.class);
            query.setParameter("ids", ids);
            List<ProductOrder> products = query.getResultList();
            // check we have the same number
            if(products == null || products.size() != ids.size()) {
                throw new EIException("Could not retrieve all products");
            }
            Price totalPrice = new Price();
            for(Price price : productsPrices.values()) {
                totalPrice.add(price);
            }
            Order order = em.find(Order.class, orderId);
            if(order == null) {
                throw new EIException("Could not find order with ID " + orderId);
            }
            // get the rate table
            RateTable rateTable = ServerUtil.getCurrencyRateTable();
            for(ProductOrder productOrder : products) {
                // make sure they are all from teh same imageSupplierOrder and from this user too
                if(productOrder.getOrder().getOwner() != dbUser) {
                    throw new EIException("Not authorized");
                }
                if(productOrder.getOrder() != order) {
                    throw new EIException("Products belong to different orders.");
                }
                // now check validity of status change
                if(productOrder.getStatus() != PRODUCTORDER_STATUS.Quoted) {
                    throw new EIException("Product order #" + productOrder.getId() + " cannot be ordered");
                }
                // check prices match as someone could be tampering the API and sending cheaper prices
                Price dbProductPrice = new Price(productOrder.getOfferedPrice(), productOrder.getCurrency());
                Price submittedPrice = productsPrices.get(productOrder.getId());
                // convert with the current rate
                Price convertedProductPrice = rateTable.getConvertedPrice(dbProductPrice, submittedPrice.getCurrency());
                // make sure we have a difference of less than 2%
                if(Math.abs((submittedPrice.getValue() - convertedProductPrice.getValue()) / convertedProductPrice.getValue()) > 0.02) {
                    throw new EIException("The amount requested is not correct, please make sure your currency rates are updated");
                }
            }
            List<String> comments = ListUtil.mutate(products, new ListUtil.Mutate<ProductOrder, String>() {
                @Override
                public String mutate(ProductOrder productOrder) {
                    ProductRequest product = productOrder.getProductRequest();
                    return "(#" + productOrder.getId() + ", sensor is " + product.getSatelliteName() + " " + product.getSensorName() + " " + ", at " + product.getStart() + ")";
                }
            });
            String comment = "Purchase for product" + (products.size() > 1 ? "s " : " ") +
                    StringUtils.join(comments, ", ") +
                    " in Order '" + order.getName() + "'";

            List<ProductOrder> planetFetchedProducts = new ArrayList<>();
            // change product order status and set prices
            for(ProductOrder productOrder : products) {
                // special handling if automated
                if(PublishAPIUtils.isPlanetOrdered(productOrder.getProductRequest().getSatelliteName())) {
                    productOrder.setStatus(PRODUCTORDER_STATUS.InProduction);
                    planetFetchedProducts.add(productOrder);
                } else {
                    // set status to paid
                    productOrder.setStatus(PRODUCTORDER_STATUS.Accepted);
                }
                // set paid price to the paid price
                productOrder.setPaidPrice(productsPrices.get(productOrder.getId()));
                productOrder.setPaidDate(new Date());
                // update the image supplier order as well
                order.setLastUpdate(new Date());
            }
            order.setLastUpdate(new Date());

            // check payment first
            // check we have enough money
            Price currentCredit = new Price(dbUser.getCredit().getCurrent(), dbUser.getCredit().getCurrency());
            if(currentCredit.getCurrency().contentEquals(totalPrice.getCurrency()) && currentCredit.getValue() > totalPrice.getValue()) {
                currentCredit.setValue(dbUser.getCredit().getCurrent() - totalPrice.getValue());
            } else {
                throw new Exception("Not enough credits left on your account");
            }
            Transaction debitTransaction = TransactionsHelper.addUserTransaction(em, dbUser, TRANSACTION_TYPE.purchase, (-1) * totalPrice.getValue(), totalPrice.getCurrency(), comment, new Date());
            debitTransaction.setOrder(order);

            NotificationSocket.notifyCreditsUpdated(dbUser);

            // Planet ordered products
            for(ProductOrder productOrder : planetFetchedProducts) {
                // delay the call to the state machine
                ProductFetchTask productFetchTask = PublishAPIUtils.createProductFetchTask(productOrder);
                productFetchTask.setStatus(STATUS.planetCreated);
                em.persist(productFetchTask);
                try {
                    NotificationSocket.notifyProductOrderStatusChanged(productOrder);
                } catch (Exception e) {
                    logger.error("Failed to send notification " + e.getMessage(), e);
                }
            }

            em.getTransaction().commit();

            try {
                MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.CONSUMER);
                mailContent.addLine("Dear " + dbUser.getFirstName() + " " + dbUser.getLastName() + ",");
                mailContent.addLine("Thank you for ordering products from EarthImages.");
                mailContent.addLine("Your prepaid account has been charged an amount of " + Utils.displayPrice(totalPrice));
                mailContent.addLine("Here are the products you have paid for:");
                for (String message : comments) {
                    mailContent.addLine(message);
                }
                mailContent.addLine("We will now proceed with your order. You will be notified of changes and progress made.");
                mailContent.addAction("view and follow your product requests", null, Utils.getSettings().getWebsiteUrl() + "#event:eventid=" + orderId);
                mailContent.sendAsynchronousEmail(dbUser, "Thank you for your purchase", "orders@geocento.com");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                reportInSlack(e);
            }
            // return the transaction
            return TransactionsHelper.convertTransaction(debitTransaction);
        } catch(Exception e) {
            handleException(em, e);
            throw new EIException(e.getMessage());
        } finally {
            em.close();
        }
    }

    @Override
    public PaymentTransactionDTO addFundsWithCreditCard(Price amount, CreditCardRequest creditCardRequest) throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            // check product order exists, that it belongs to the user, that payment has been requested and that the price match
            em.getTransaction().begin();
            // retrieve user first
            User dbUser = em.find(User.class, logUserName);
            Price paidAmount = new Price(amount.getValue(), amount.getCurrency());
            boolean chargeVAT = dbUser.isChargeVAT();
            if(chargeVAT) {
                paidAmount.setValue(amount.getValue() * 1.2);
            }
            // create transaction
            String token = creditCardRequest.getToken();
            String cvv = creditCardRequest.getCvv();
            String transactionId;
            String comment = "Adding funds to your EarthImages account";
            if (token == null) {
                String number = creditCardRequest.getNumber();
                String month = creditCardRequest.getMonth();
                String year = creditCardRequest.getYear();
                boolean storeCard = creditCardRequest.isStoreCard();
                transactionId = PaymentUtil.getInstance().makeCreditCardPayment(dbUser.getUsername(), paidAmount.getValue(), paidAmount.getCurrency(), number, cvv, month, year, comment, storeCard);
            } else {
                transactionId = PaymentUtil.getInstance().makeCreditCardPayment(dbUser.getUsername(), paidAmount.getValue(), paidAmount.getCurrency(), token, cvv, comment);
            }
            // log 2 transactions, one for adding funds and one for paying
            Transaction creditTransaction = TransactionsHelper.addUserTransaction(em, dbUser, TRANSACTION_TYPE.creditCard, amount.getValue(), amount.getCurrency(),
                    "Added funds from credit card" + (chargeVAT ? " (+ 20% VAT)" : ""), new Date());
            creditTransaction.setBraintreeTransactionId(transactionId);

            em.getTransaction().commit();

            try {
                MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.CONSUMER);
                mailContent.addLine("Dear " + dbUser.getFirstName() + " " + dbUser.getLastName() + ",");
                mailContent.addLine("Thank you for adding funds to EarthImages. We have updated your prepaid account. " +
                        "Your new balance is " + Utils.displayPrice(new Price(dbUser.getCredit().getCurrent(), dbUser.getCredit().getCurrency())));
                mailContent.addLine("Your credit card was charged " + Utils.displayPrice(paidAmount) +
                        (chargeVAT ? " (including 20% VAT)" : "") +
                ".");
                mailContent.sendAsynchronousEmail(dbUser, "Thank you for your purchase", "orders@geocento.com");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            // create invoice
            try {
                em.getTransaction().begin();
                Invoice invoice = createInvoice(dbUser, amount, creditTransaction.getId() + "");
                creditTransaction.setInvoiceId(invoice.getInvoiceID());
                em.getTransaction().commit();
                try {
                    SlackAPIHelper.sendMessageWebhookNoException(Utils.getSettings().getPrivateSlackHook(),
                            "*New funds added on EI NEO* - " + Utils.displayPrice(amount) + " from user " + dbUser.getUsername() + "<" + XeroAPIUtil.getInvoiceLink(invoice) + "|View Invoice>");
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            NotificationSocket.notifyCreditsUpdated(dbUser);
            // return the transaction
            return TransactionsHelper.convertTransaction(creditTransaction);
        } catch(Exception e) {
            handleException(em, e);
            SlackAPIHelper.sendMessageWebhookNoException(Utils.getSettings().getPrivateSlackHook(),
                    "*Error adding funds on EI NEO* - with user " + logUserName + ", error is " + e.getMessage());
            throw new EIException(e.getMessage());
        } finally {
            em.close();
        }
    }

    private Invoice createInvoice(User user, Price fundTransferred, String transactionId) throws Exception {
        Contact contact = XeroAPIUtil.findContact(user.getEmail());
        if (contact == null) {
            contact = XeroAPIUtil.createContact(user.getEmail(), user.getFirstName() + " " + user.getLastName(),
                    user.getPhone(),
                    user.getAddress(), user.getCountryCode(),
                    user.getCompany());
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Invoice invoice = new Invoice();
        invoice.setType(InvoiceType.ACCREC);
        invoice.setReference("EarthImages NEO funds purchase #" + transactionId);
        //invoice.setInvoiceNumber();
        invoice.setContact(contact);
        invoice.setCurrencyCode(CurrencyCode.fromValue(fundTransferred.getCurrency()));
        invoice.setAmountDue(new BigDecimal(fundTransferred.getValue()));
        invoice.setDueDate(calendar);
        invoice.setBrandingThemeID(Utils.getSettings().getXeroBrandingThemeID());
        invoice.setDate(calendar);
        ArrayOfLineItem lineItems = new ArrayOfLineItem();
        LineItem lineItem = new LineItem();
        lineItem.setDescription("Purchase of funds for EarthImages NEO (excluding VAT)");
        BigDecimal unitAmount = new BigDecimal(fundTransferred.getValue());
        // reduce to 2 decimals max
        unitAmount.setScale(2, BigDecimal.ROUND_UP);
        invoice.setLineAmountTypes(user.isChargeVAT() ? LineAmountType.EXCLUSIVE : LineAmountType.NO_TAX);
        lineItem.setUnitAmount(unitAmount);
        if(user.isChargeVAT()) {
            BigDecimal taxAmount = new BigDecimal(0.2 * fundTransferred.getValue());
            lineItem.setTaxAmount(taxAmount);
            lineItem.setTaxType("OUTPUT2");
            invoice.setTotalTax(taxAmount);
        }
        lineItem.setAccountCode("001");
        lineItems.getLineItem().add(lineItem);
        invoice.setLineItems(lineItems);
        invoice.setAmountPaid(unitAmount);
        invoice.setFullyPaidOnDate(calendar);
        invoice.setStatus(InvoiceStatus.AUTHORISED);
        Invoice createdInvoice = XeroAPIUtil.createInvoice(invoice);
        // now make payment on Xero
        XeroAPIUtil.payInvoice(createdInvoice);
        return createdInvoice;
    }

    @Override
    public List<CartProductDTO> loadCartRequestDTO() throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            UserCart cart = user.getUserCart();
            if(cart == null) {
                return new ArrayList<CartProductDTO>();
            }
            return getCartProducts(cart);
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private List<CartProductDTO> getCartProducts(List<ProductRequest> productRequests, List<TaskingRequest> newTaskingRequests) {
        List<CartProductDTO> cartProducts = new ArrayList<CartProductDTO>();
        cartProducts.addAll(ListUtil.mutate(productRequests, productRequest -> {
            CartProductDTO cartProductDTO = new CartProductDTO();
            cartProductDTO.setType(TYPE.ARCHIVE);
            cartProductDTO.setProductId(productRequest.getGeocentoid());
            cartProductDTO.setSatelliteName(productRequest.getSatelliteName());
            cartProductDTO.setSensorName(productRequest.getSensorName());
            cartProductDTO.setStart(productRequest.getStart());
            cartProductDTO.setCoordinatesWKT(productRequest.getCoordinatesWKT());
            cartProductDTO.setProductRequestId(productRequest.getId());
            return cartProductDTO;
        }));
        cartProducts.addAll(ListUtil.mutate(newTaskingRequests, taskingRequest -> {
            CartProductDTO cartProductDTO = new CartProductDTO();
            cartProductDTO.setType(TYPE.TASKING);
            cartProductDTO.setProductRequestId(taskingRequest.getId());
            cartProductDTO.setProductId(taskingRequest.getProductId());
            ProductEntity productEntity = taskingRequest.getProductEntity();
            cartProductDTO.setSatelliteName(productEntity.getSatelliteName());
            cartProductDTO.setSensorName(productEntity.getInstrumentName());
            cartProductDTO.setStart(productEntity.getStart());
            cartProductDTO.setCoordinatesWKT(productEntity.getCoordinates());
            return cartProductDTO;
        }));
        return cartProducts;
    }

    @Override
    public List<CartProductDTO> addProductsToCart(String searchId, AOI aoi, List<Product> products) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, logUserName);
            UserCart cart = user.getUserCart();
            if(cart == null) {
                cart = new UserCart();
                cart.setOwner(user);
                em.persist(cart);
            }
            // force saving of AoI
            if(aoi == null) {
                aoi = new AOIPolygon();
                aoi.setName("Full selection");
                ((AOIPolygon) aoi).setPoints(ListUtil.toList(EOLatLng.parseWKT(products.get(0).getCoordinatesWKT().replace("POLYGON((", "").replace("))", ""))));
            }
            aoi.setId(null);
            em.persist(aoi);
            // get the user cart to add the new product requests to it
            List<ProductRequest> newProductRequests = new ArrayList<ProductRequest>();
            for(Product product : getCatalogueProducts(products)) {
                ProductRequest productRequest = OrderHelper.createProductRequest(searchId, aoi, product);
                // add to the user cart for saving
                cart.addProductRequest(productRequest);
                productRequest.setUserCart(cart);
                em.persist(productRequest);
                // keep a separate list to return the new product requests added
                newProductRequests.add(productRequest);
            }
            List<TaskingRequest> newTaskingRequests = new ArrayList<TaskingRequest>();
            for(Product product : getFutureProducts(products)) {
                TaskingRequest taskingRequest = OrderHelper.createTaskingRequest(searchId, aoi, product);
                // add to the user cart for saving
                cart.addTaskingRequest(taskingRequest);
                taskingRequest.setUserCart(cart);
                em.persist(taskingRequest);
                // keep a separate list to return the new product requests added
                newTaskingRequests.add(taskingRequest);
            }
            em.getTransaction().commit();
            // TODO - find a way to only send to the other websocket sessions?
            NotificationSocket.notifyCartProductsChanged(logUserName, getCartProducts(user.getUserCart()));
            // send back the new list of cart products
            List<CartProductDTO> newCartProducts = getCartProducts(newProductRequests, newTaskingRequests);
            return newCartProducts;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private List<Product> getCatalogueProducts(List<Product> products) {
        return ListUtil.filterValues(products, value -> value.getType() == TYPE.ARCHIVE);
    }

    private List<Product> getFutureProducts(List<Product> products) {
        return ListUtil.filterValues(products, value -> value.getType() == TYPE.TASKING);
    }

    @Override
    public List<Long> removeProductsFromCart(final List<Long> productRequestIds) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, logUserName);
            UserCart cart = user.getUserCart();
            if(cart == null) {
                throw new EIException("Server error");
            }
            List<Long> requestIds = new ArrayList<Long>();
            List<ProductRequest> productsToRemove = ListUtil.filterValues(cart.getProductRequests(), new ListUtil.CheckValue<ProductRequest>() {
                @Override
                public boolean isValue(ProductRequest value) {
                    return productRequestIds.contains(value.getId());
                }
            });
            cart.getProductRequests().removeAll(productsToRemove);
            for(ProductRequest productRequest : productsToRemove) {
                productRequest.setUserCart(null);
                em.remove(productRequest);
                requestIds.add(productRequest.getId());
            }
            List<TaskingRequest> taskingRequestsToRemove = ListUtil.filterValues(cart.getTaskingRequests(), value -> productRequestIds.contains(value.getId()));
            cart.getTaskingRequests().removeAll(taskingRequestsToRemove);
            for(TaskingRequest taskingRequest : taskingRequestsToRemove) {
                taskingRequest.setUserCart(null);
                em.remove(taskingRequest);
                requestIds.add(taskingRequest.getId());
            }
            em.getTransaction().commit();
            NotificationSocket.notifyCartProductsChanged(logUserName, getCartProducts(user.getUserCart()));
            return requestIds;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private List<CartProductDTO> getCartProducts(UserCart userCart) {
        return getCartProducts(userCart.getProductRequests(), userCart.getTaskingRequests());
    }

    @Override
    public AoIProductsDTO loadAoIAndProducts(String searchId, List<String> productsIds) throws EIException {
        AoIProductsDTO aoiProductsDTO = new AoIProductsDTO();
        try {
            if(searchId != null) {
                try {
                    // look for search
                    SearchRequest search = EIAPIUtil.loadSearchRequest(searchId);
                    aoiProductsDTO.setAoi(AOIUtils.fromWKT(search.getAoiWKT()));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            List<Product> products = new ArrayList<Product>();
            List<String> geocentoProductIds = ListUtil.filterValues(productsIds, value -> isGeocentoId(value));
            if(geocentoProductIds.size() > 0) {
                products.addAll(EIAPIUtil.queryProductsById(StringUtils.join(geocentoProductIds, ","), true));
            }
            List<String> supplierProductIds = ListUtil.filterValues(productsIds, value -> !isGeocentoId(value));
            if(supplierProductIds.size() > 0) {
                products.addAll(EIAPIUtil.queryProductsById(StringUtils.join(supplierProductIds, ","), false));
            }
            aoiProductsDTO.setProducts(products);
            // we are missing the supplier name from the EI API
/*
            List<Long> instrumentIds = ListUtil.mutate(products, new ListUtil.Mutate<Product, Long>() {
                @Override
                public Long mutate(Product product) {
                    return product.getInstrumentId();
                }
            });
            final List<Policy> policies = EIAPIUtil.getCataloguePoliciesInstrument(instrumentIds);
            for(Product product : products) {
                Policy policy = ListUtil.findValue(policies, new ListUtil.CheckValue<Policy>() {
                    @Override
                    public boolean isValue(Policy value) {
                        return value.getInstrumentIds().contains(product.getInstrumentId());
                    }
                });
                product.setProviderName(policy.getProviderName());
            }
            aoiProductsDTO.setPolicies(policies);
*/
            return aoiProductsDTO;
        } catch(Exception e) {
            if(e instanceof EIException) {
                throw (EIException) e;
            }
            logger.error(e.getMessage(), e);
            throw new EIException("Server error.");
        }
    }

    private boolean isGeocentoId(String productId) {
        boolean geocentoIds = productId.length() == 32;
        // deal with special case of Planet
        if(productId.startsWith("PL_")) {
            geocentoIds = true;
        }
        return geocentoIds;
    }

    private void validateCookie(String cartId) throws EIException {
        Cookie[] cookies = getThreadLocalRequest().getCookies();
        if(cartId == null) {
            throw new EIException("No cartid provided");
        }
        if(cookies == null) {
            throw new EIException("No cookies provided");
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equalsIgnoreCase("EINEOID") && cookie.getValue().equalsIgnoreCase(cartId)) {
                // TODO - check matches a time based token
                return;
            }
        }
        throw new EIException("Not allowed");
    }

    @Override
    public void saveCoverageRequest(String email, String additionalInformation, String searchId) throws EIException {
        try {
            // send email
            MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.ORDER);
            mailContent.addTitle("New request for coverage quotation");
            mailContent.addLine("User email address is " + email);
            mailContent.addLine("Additional information is '" + additionalInformation + "'");
            mailContent.addAction("View search", "View the user's search", Utils.getSettings().getWebsiteUrl() + "/#mapviewer:query=" + searchId);
            mailContent.sendEmail(Utils.getSettings().getOrderEmail(), "New request for coverage quotation");
        } catch (Exception e) {
            logger.error("Failed to send email, reason is " + e.getMessage());
            throw new EIException("Failed to send the request");
        }
    }

    @Override
    public EventDTO loadOrder(String orderId, String password) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(orderId == null) {
            throw new EIException("Order id missing");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            Order order = em.find(Order.class, orderId);
            if(order == null) {
                throw new EIException("Could not find order with id " + orderId);
            }
            checkOrderUser(order, user);
            EventDTO eventDTO = new EventDTO();
            eventDTO.setId(orderId);
            eventDTO.setName(order.getName());
            eventDTO.setStatus(order.getStatus());
            // call the publisher to get the information on products
            // sort product orders by creation date first
            Collections.sort(order.getProductOrders(), new Comparator<ProductOrder>() {
                @Override
                public int compare(ProductOrder o1, ProductOrder o2) {
                    return o1.getCreationTime() == null ? -1 :
                            o2.getCreationTime() == null ? 1 :
                            o2.getCreationTime().compareTo(o1.getCreationTime());
                }
            });
            eventDTO.setProductOrders(ListUtil.mutate(order.getProductOrders(), new ListUtil.Mutate<ProductOrder, ProductOrderDTO>() {
                @Override
                public ProductOrderDTO mutate(ProductOrder productOrder) {
                    ProductOrderDTO productOrderDTO = convertProductOrderDTO(productOrder);
                    // check for additional data needed
                    if(productOrder.getStatus() == PRODUCTORDER_STATUS.Documentation && productOrder.getProductRequest() != null) {
                        productOrderDTO.setPolicyId(productOrder.getPolicyId());
                        // find product fetch task to get the ei order id
                        TypedQuery<ProductFetchTask> query = em.createQuery("select p from ProductFetchTask p where p.productOrder = :productOrder", ProductFetchTask.class);
                        query.setParameter("productOrder", productOrder);
                        List<ProductFetchTask> productFetchTasks = query.getResultList();
                        if(productFetchTasks.size() == 1) {
                            productOrderDTO.setEIOrderId(productFetchTasks.get(0).getEIOrderId());
                        }
                    }
                    // add converted offered price
                    if(productOrderDTO.getOfferedPrice() != null) {
                        try {
                            productOrderDTO.setConvertedOfferedPrice(ServerUtil.getCurrencyRateTable().getConvertedPrice(productOrderDTO.getOfferedPrice(), user.getCredit().getCurrency()));
                        } catch (EIException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    return productOrderDTO;
                }
            }));
            // add the required license agreements
            List<ProductOrder> productOrdersLicenseRequired = ListUtil.filterValues(order.getProductOrders(), value -> {return value.getStatus() == PRODUCTORDER_STATUS.Documentation;});
            HashMap<String, EULARequest> eulasRequestedMatch = new HashMap<String, EULARequest>();
            if(productOrdersLicenseRequired.size() > 0) {
                // we need to add a eula request for each eula document id and each order id
                Set<Long> policyIds = ListUtil.mutate(new HashSet<ProductOrder>(productOrdersLicenseRequired), productOrder -> productOrder.getPolicyId());
                for(Long policyId : policyIds) {
                    try {
                        // load teh policy to get the eulas
                        ProductPolicy productPolicy = EIAPIUtil.getPolicy(policyId);
                        // now group by EI order id and eula document id
                        List<ProductOrder> productOrders = ListUtil.filterValues(productOrdersLicenseRequired, value -> value.getPolicyId() == policyId);
                        for(ProductOrder productOrder : productOrders) {
                            // need the productOrderDTO to find the EI Order ID...
                            ProductOrderDTO productOrderDTO = ListUtil.findValue(eventDTO.getProductOrders(), value -> value.getId().equals(productOrder.getId()));
                            EULADocumentDTO eulaDocumentDTO = PolicyHelper.getLicensingEULADocument(productPolicy.getLicensingPolicy(), productOrder.getLicenseOption().getPropertyValue());
                            String eiOrderId = productOrderDTO.getEIOrderId();
                            String value = eiOrderId + "_" + eulaDocumentDTO.getId();
                            if(!eulasRequestedMatch.containsKey(value)) {
                                EULARequest eulaRequest = new EULARequest();
                                eulaRequest.setEiOrderId(eiOrderId);
                                eulaRequest.setEulaName(eulaDocumentDTO.getName());
                                eulaRequest.setProductPolicyId(policyId);
                                LicensingPolicy licensingPolicy = productPolicy.getLicensingPolicy();
                                eulaRequest.setLicensingPolicyId(licensingPolicy.getId());
                                EULADocumentDTO eulaDocument = PolicyHelper.getLicensingEULADocument(licensingPolicy, productOrder.getLicenseOption().getPropertyValue());
                                eulaRequest.setEULADocumentId(eulaDocument.getId());
                                eulasRequestedMatch.put(value, eulaRequest);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        throw new EIException("Could not retrieve licensing policy information");
                    }
                }
                eventDTO.setEulasRequested(new ArrayList<EULARequest>(eulasRequestedMatch.values()));
            }
            return eventDTO;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private void checkOrderUser(Order order, User dbUser) throws EIException {
        if(dbUser.getUserRole() != USER_ROLE.ADMINISTRATOR && !order.getOwner().getUsername().contentEquals(dbUser.getUsername())) {
            throw new EIException("Access not allowed");
        }
    }

    private ProductOrderDTO convertProductOrderDTO(ProductOrder productOrder) {
        ProductOrderDTO productOrderDTO = new ProductOrderDTO();
        productOrderDTO.setId(productOrder.getId());
        productOrderDTO.setStatus(productOrder.getStatus());
        productOrderDTO.setEstimatedDeliveryDate(productOrder.getEstimatedDeliveryTime());
        productOrderDTO.setDeliveredDate(productOrder.getDeliveredTime());
        productOrderDTO.setPublicationStatus(productOrder.getPublicationStatus());
        productOrderDTO.setAOI(productOrder.getAoi());
        try {
            productOrderDTO.setCoordinates(EOLatLng.parseWKT(StringUtils.extract(productOrder.getSelectionGeometry(), "((", "))")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        productOrderDTO.setDescription(productOrder.getTitle());
        productOrderDTO.setInfo(productOrder.getDescription());
        productOrderDTO.setLabel(productOrder.getLabel());
        boolean isCatalogue = productOrder.getProductRequest() != null;
        if(isCatalogue) {
            productOrderDTO.setOriginalProductAcquisitionTime(productOrder.getProductRequest().getStart());
            productOrderDTO.setOriginalProductId(productOrder.getProductRequest().getGeocentoid());
        } else {
            productOrderDTO.setOriginalProductAcquisitionTime(productOrder.getTaskingRequest().getProductEntity().getStart());
            productOrderDTO.setOriginalProductId(productOrder.getTaskingRequest().getProductId());
        }
        if(productOrder.getFileLocation() != null) {
            try {
                File productFile = new File(productOrder.getFileLocation());
                productOrderDTO.setProductFileName(productFile.getName());
                productOrderDTO.setProductFileSizeBytes(productFile.length());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        try {
            productOrderDTO.setDownloadManualURL(ServerUtil.getServiceUrl("api/download-product/download/" + productOrder.getPolicyId() + "/manual/"));
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        productOrderDTO.setThumbnailURL(productOrder.getThumbnailURL());
        try {
            if(productOrder.getPublishProductRequests().size() > 0) {
                // only send the first one which is the display version
                ProductPublishRequest publishRequest = productOrder.getPublishProductRequests().get(0);
                PublishProcessProducts publishProducts = ProductPublisherAPIUtil.getProductsPublished(publishRequest.getPublishTaskId());
                productOrderDTO.setProductWMSServiceURL(Utils.getSettings().getProductServiceWMSURL().replace("$userName", publishProducts.getWorkspace()));
                ArrayList<ProductMetadataDTO> products = new ArrayList<ProductMetadataDTO>();
                for (ProductMetadata productMetadata : publishProducts.getGeneratedProductMetadas()) {
                    products.add(PublishAPIUtils.convertProductMetadata(productMetadata, publishRequest));
                }
                productOrderDTO.setPublishedProducts(products);
            }
            productOrderDTO.setWorkspaces(ListUtil.mutate(productOrder.getWorkspaces(), new ListUtil.Mutate<Workspace, WorkspaceSummaryDTO>() {
                @Override
                public WorkspaceSummaryDTO mutate(Workspace workspace) {
                    WorkspaceSummaryDTO workspaceSummaryDTO = new WorkspaceSummaryDTO();
                    workspaceSummaryDTO.setName(workspace.getName());
                    workspaceSummaryDTO.setId(workspace.getId());
                    return workspaceSummaryDTO;
                }
            }));
            if(productOrder.getCurrency() != null) {
                productOrderDTO.setTotalPrice(new Price(productOrder.getTotalPrice(), productOrder.getCurrency()));
            }
            if(productOrder.getOfferedPrice() != null) {
                productOrderDTO.setOfferedPrice(new Price(productOrder.getOfferedPrice(), productOrder.getCurrency()));
            }
            if(productOrder.getPaidPrice() != null) {
                productOrderDTO.setPaidPrice(productOrder.getPaidPrice());
            }
            // set options and comments
            if(productOrder.getLicenseOption() != null) {
                productOrderDTO.setLicense(productOrder.getLicenseOption().getPropertyValue());
            }
            productOrderDTO.setParameters(ListUtil.mutate(productOrder.getParameters(), value -> {
                UserOrderParameterDTO userOrderParameterDTO = new UserOrderParameterDTO();
                userOrderParameterDTO.setId(value.getId());
                userOrderParameterDTO.setName(value.getName());
                userOrderParameterDTO.setValue(value.getPropertyValue());
                return userOrderParameterDTO;
            }));
            productOrderDTO.setComments(productOrder.getComments());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return productOrderDTO;
    }

    @Override
    public void cancelOrder(String id) throws EIException {

    }

    @Override
    public void archiveOrder(String orderId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(orderId == null) {
            throw new EIException("Order id missing");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            Order order = em.find(Order.class, orderId);
            if(order == null) {
                throw new EIException("Could not find order with id " + orderId);
            }
            checkOrderUser(order, user);
            em.getTransaction().begin();
            order.setStatus(ORDER_STATUS.ARCHIVED);
            em.getTransaction().commit();

        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteOrder(String orderId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(orderId == null) {
            throw new EIException("Order id missing");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            Order order = em.find(Order.class, orderId);
            if(order == null) {
                throw new EIException("Could not find order with id " + orderId);
            }
            checkOrderUser(order, user);
            for(ProductOrder productOrder : order.getProductOrders())
                deleteProduct(productOrder.getId());

            em.getTransaction().begin();
            em.remove(order);
            em.getTransaction().commit();

        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteProduct(String productOrderId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(productOrderId == null) {
            throw new EIException("Product id missing");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            if(productOrder == null) {
                throw new EIException("Could not find product with id " + productOrderId);
            }
            checkOrderUser(productOrder.getOrder(), user);

            em.getTransaction().begin();

            //Deleting productfetchtasks
            TypedQuery<ProductFetchTask> query = em.createQuery("select p from ProductFetchTask p where p.productOrder.id = :productId", ProductFetchTask.class);
            query.setParameter("productId", productOrder.getId());
            List<ProductFetchTask> productFetchTasks = query.getResultList();

            for (ProductFetchTask productFetchTask : productFetchTasks)
            {
                Long downloadTaskId = productFetchTask.getDownloadTaskId();
                Long publishTaskId = productFetchTask.getPublishTaskId();
                em.remove(productFetchTask);
                //Sending delete product to Publish API
                if(downloadTaskId != null)
                    ProductDownloaderAPIUtil.deleteProduct(downloadTaskId);
                if(publishTaskId != null)
                    ProductPublisherAPIUtil.deleteProduct(publishTaskId);
            }

            Order order = productOrder.getOrder();
            order.getProductOrders().remove(productOrder);
            em.remove(productOrder);
            OrderHelper.updateOrderStatus(order);
            em.getTransaction().commit();


            //Trying to delete the product from the disk
            if(productOrder.getFileLocation() != null) {
/*
                File productDirectory = new File(productOrder.getFileLocation()).getParentFile();
                FileUtils.deleteDirectory(productDirectory);
*/
                new File(productOrder.getFileLocation()).delete();
            }

            NotificationSocket.notifyOrderChanged(user, order);

        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }


    @Override
    public List<EventSummaryDTO> loadOrders() throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            boolean isAdministrator = false; //user.getUserRole() == USER_ROLE.ADMINISTRATOR;
            TypedQuery<Order> query = em.createQuery("select o from orders o " +
                    "where o.status <> :status " +
                            (isAdministrator ? "" : "and o.owner = :owner ") +
                    "order by o.lastUpdate DESC", Order.class);
            query.setParameter("status", ORDER_STATUS.ARCHIVED);
            if(!isAdministrator) {
                query.setParameter("owner", user);
            }
            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<Order, EventSummaryDTO>() {
                @Override
                public EventSummaryDTO mutate(Order order) {
                    EventSummaryDTO eventSummaryDTO = new EventSummaryDTO();
                    eventSummaryDTO.setId(order.getId());
                    eventSummaryDTO.setName(order.getName());
                    eventSummaryDTO.setDescription(order.getDescription());
                    eventSummaryDTO.setCreationDate(order.getCreationTime());
                    eventSummaryDTO.setStatus(order.getStatus());
                    eventSummaryDTO.setAois(getAoisFromProductOrders(order.getProductOrders()));
                    eventSummaryDTO.setBounds(getBoundFromProductOrders(order.getProductOrders()));
                    eventSummaryDTO.setNumOfProducts(order.getProductOrders().size());
                    return eventSummaryDTO;
                }
            });
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<EventSummaryDTO> loadOrders(String name, Date start, Date stop, String status) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        if(start.getTime() > stop.getTime())
        {
            throw new EIException("Start date cannot be grater than end date");
        }

        if(start.getTime() > new Date().getTime())
        {
            throw new EIException("Start date cannot be grater than current date");
        }

        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            boolean isAdministrator = false; //user.getUserRole() == USER_ROLE.ADMINISTRATOR;

            TypedQuery<Order> query;

            String queryString = "select o from orders o where  " +
                    "o.creationTime between :start and :stop  ";
            if(name != null && name.length() > 0) {
                queryString += " and lower(o.name) like '%" + name.toLowerCase() + "%' ";
            }
            boolean hasStatus = !status.equals("ALL");
            if(!hasStatus) {
                queryString += " and o.status <> :status ";
            } else {
                queryString += " and o.status = :status ";
            }
            if(!isAdministrator) {
                queryString += "and o.owner = :owner ";
            }

            queryString += " order by o.lastUpdate DESC ";

            query = em.createQuery(queryString, Order.class);

            query.setParameter("start", start);
            query.setParameter("stop", stop);
            if(!hasStatus) {
                query.setParameter("status", ORDER_STATUS.ARCHIVED);
            } else {
                query.setParameter("status", ORDER_STATUS.valueOf(status));
            }
            if(!isAdministrator) {
                query.setParameter("owner", user);
            }

            return ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<Order, EventSummaryDTO>() {
                @Override
                public EventSummaryDTO mutate(Order order) {
                    EventSummaryDTO eventSummaryDTO = new EventSummaryDTO();
                    eventSummaryDTO.setId(order.getId());
                    eventSummaryDTO.setName(order.getName());
                    eventSummaryDTO.setDescription(order.getDescription());
                    eventSummaryDTO.setCreationDate(order.getCreationTime());
                    eventSummaryDTO.setStatus(order.getStatus());
                    // this totally redundant, if you have the AoIs why do you need the bounds and the number of products
                    // TODO - replace with setProductOrderBounds(List
                    eventSummaryDTO.setBounds(getBoundFromProductOrders(order.getProductOrders()));
                    eventSummaryDTO.setAois(getAoisFromProductOrders(order.getProductOrders()));
                    eventSummaryDTO.setNumOfProducts(order.getProductOrders().size());
                    return eventSummaryDTO;
                }
            });
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private List<AOI> getAoisFromProductOrders(List<ProductOrder> productOrders)
    {
        List<AOI> aois = new ArrayList<AOI>();

        for (ProductOrder productOrder : productOrders)
        {
            aois.add(productOrder.getAoi());
        }

        return aois;
    }

    private EOBounds getBoundFromProductOrders(List<ProductOrder> productOrders)
    {
        double minx = 180;
        double maxx = -180;
        double miny = 90;
        double maxy= -90;

        for (ProductOrder productOrder : productOrders)
        {
            try {
                EOBounds bounds = productOrder.getAoi() != null ? AOIUtils.getBounds(productOrder.getAoi()) :
                        AOIUtils.getBounds(AOIUtils.fromWKT(productOrder.getSelectionGeometry()));
                EOLatLng sw = bounds.getCoordinatesSW();
                EOLatLng ne = bounds.getCoordinatesNE();

                if(sw.getLng() < minx)
                    minx = sw.getLng();

                if(sw.getLat() < miny)
                    miny = sw.getLat();

                if(ne.getLng() > maxx)
                    maxx = ne.getLng();

                if(ne.getLat() > maxy)
                    maxy = ne.getLat();

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        EOLatLng southWest = new EOLatLng(miny,minx);
        EOLatLng northEast = new EOLatLng(maxy,maxx);

        return new EOBounds(northEast, southWest);
    }

    @Override
    public List<CreditCardToken> getUserPaymentInformation() throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());
        // if user does not exist, create user
        EntityManager em = EMF.get().createEntityManager();
        try {
            User dbUser = em.find(User.class, logUserName);
            // check user registered
            if(!PaymentUtil.getInstance().checkCustomerRegistered(logUserName)) {
                PaymentUtil.getInstance().registerCustomer(logUserName, dbUser.getFirstName(), dbUser.getLastName(), dbUser.getCompany(), dbUser.getEmail());
            } else {
                // check for existing credit cards
                return PaymentUtil.getInstance().getUserCreditCards(logUserName);
            }
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException("Could not retrieve user payment information");
        } finally {
            em.close();
        }
        return null;
    }

    @Override
    public UserInformationDTO loadUserInformation() throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());
        // if user does not exist, create user
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            UserInformationDTO userInformationDTO = new UserInformationDTO();
            userInformationDTO.setUsername(user.getUsername());
            userInformationDTO.setFirstName(user.getFirstName());
            userInformationDTO.setLastName(user.getLastName());
            userInformationDTO.setEmail(user.getEmail());
            userInformationDTO.setAddress(user.getAddress());
            userInformationDTO.setCompany(user.getCompany());
            userInformationDTO.setCountryCode(user.getCountryCode());
            userInformationDTO.setPhone(user.getPhone());
            userInformationDTO.setNeedsVATNumber(user.isNeedsVATNumber());
            userInformationDTO.setCommunityVATNumber(user.getCommunityVATNumber());
            return userInformationDTO;
        } catch (Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<PaymentTransactionDTO> getTransactionsHistory(int transactionsHistoryStart, int transactionsHistoryPageSize) throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());
        // if user does not exist, create user
        EntityManager em = EMF.get().createEntityManager();
        try {
            TypedQuery<Transaction> query = em.createQuery("Select t from Transaction t where t.credit.owner.username = :customer ORDER BY t.date DESC", Transaction.class);
            query.setParameter("customer", logUserName);
            return ListUtil.mutate(query.getResultList(), (ListUtil.Mutate<Transaction, PaymentTransactionDTO>) transaction -> {
                return TransactionsHelper.convertTransaction(transaction);
            });
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException("Could not retrieve user payment transactions");
        } finally {
            em.close();
        }
    }

    @Override
    public void updateUserProfile(UserInformationDTO userProfile, String password) throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());
        // if user does not exist, create user
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            String hashFromDB = user.getPasswordHash();
            boolean valid = BCrypt.checkpw(password, hashFromDB);
            if(!valid) {
                throw new EIException("Password is not valid one");
            }
            em.getTransaction().begin();
            user.setFirstName(userProfile.getFirstName());
            user.setLastName(userProfile.getLastName());
            user.setCompany(userProfile.getCompany());
            user.setAddress(userProfile.getAddress());
            user.setEmail(userProfile.getEmail());
            user.setCountryCode(userProfile.getCountryCode());
            user.setPhone(userProfile.getPhone());
            em.getTransaction().commit();
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException("Could not retrieve user payment transactions");
        } finally {
            em.close();
        }
    }

    @Override
    public List<DocumentDTO> getDocumentation() throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());
        // if user does not exist, create user
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            List<DocumentDTO> documents = new ArrayList<DocumentDTO>();
            // get licenses signed first
            TypedQuery<SignedLicense> query = em.createQuery("select s from SignedLicense s where s.owner = :user order by s.creationTime DESC", SignedLicense.class);
            query.setParameter("user", user);
            documents.addAll(ListUtil.mutate(query.getResultList(), new ListUtil.Mutate<SignedLicense, DocumentDTO>() {
                @Override
                public DocumentDTO mutate(SignedLicense signedLicense) {
                    File signedPDF = new File(signedLicense.getPath());
                    DocumentDTO documentDTO = new DocumentDTO();
                    documentDTO.setName("Signed license");
                    documentDTO.setSize(signedPDF.length());
                    try {
                        documentDTO.setDownloadUrl(ServerUtil.getServiceUrl("api/license/signed/download/" + signedLicense.getId()));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    documentDTO.setCreatedOn(signedLicense.getCreationTime());
                    return documentDTO;
                }
            }));
            return documents;
        } catch (Exception e) {
            handleException(em, e);
            throw new EIException("Could not retrieve user payment transactions");
        } finally {
            em.close();
        }
    }

    // internal layers need to be handled a certain way due to the restrictions from Geoserver
    // the main issue is that to create group layers you need to have all layers within the same workspace
    // this would expose all the sub layers to the get capabilities request
    // an alternative would be to use th image mosaic plugin?
    @Override
    public List<LayerResource> loadAvailableLayers(String resourceId) throws EIException {
        String logUserName = ServerUtil.validateUser(this.getThreadLocalRequest());
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            List<LayerResource> layers = new ArrayList<LayerResource>();
            if(resourceId == null) {
                // send the root tree
                WMSServerDTO wmsServerDTO = new WMSServerDTO();
                wmsServerDTO.setId("workspaces");
                wmsServerDTO.setName("Workspace layers");
                wmsServerDTO.setDescription("The published layers generated from your workspaces");
                //wmsServerDTO.setBaseUrl(Utils.getSettings().getProductServiceWMSURL().replace("$userName", PublisherUtils.getPublicWorkspace(user)));
                layers.add(wmsServerDTO);
                // send the root tree
                String eineoWMSServerUrl = Utils.getSettings().getWMSLayersUrl();
                if(!StringUtils.isEmpty(eineoWMSServerUrl)) {
                    WMSServerDTO earthimagesWMSServerDTO = new WMSServerDTO();
                    earthimagesWMSServerDTO.setName("EarthImages layers");
                    earthimagesWMSServerDTO.setDescription("EarthImages WMS layers");
                    String[] serverUrls = eineoWMSServerUrl.split("::");
                    earthimagesWMSServerDTO.setBaseUrl(serverUrls[0]);
                    if(serverUrls.length > 1) {
                        earthimagesWMSServerDTO.setWCSUrl(serverUrls[1]);
                    }
                    layers.add(earthimagesWMSServerDTO);
                }
            } else {
                switch (resourceId) {
                    case "workspaces": {
                        // find all user organisation workspaces published layers
                        TypedQuery<PublishedLayer> query = em.createQuery("select p from PublishedLayer p where p.workspace.owner = :user and p.published = :published order by p.name asc", PublishedLayer.class);
                        query.setParameter("user", user);
                        query.setParameter("published", true);
                        for(PublishedLayer publishedLayer : query.getResultList()) {
                            LayerDTO layerDTO = new LayerDTO();
                            layerDTO.setLayerType(LAYER_TYPE.WMS);
                            layerDTO.setName(publishedLayer.getName());
                            layerDTO.setDescription(publishedLayer.getDescription());
                            layerDTO.setBaseUrl(Utils.getSettings().getProductServiceWMSURL().replace("$userName", publishedLayer.getPublishedWorkspace()));
                            layerDTO.setLayerName(publishedLayer.getPublishedId());
                            EOBounds bounds = new EOBounds();
                            for(ProductLayer productLayer : publishedLayer.getProductLayers()) {
                                try {
                                    EOLatLng[] coordinates = EOLatLng.parseWKT(StringUtils.extract(productLayer.getBounds(), "((", "))"));
                                    bounds.extend(EOBounds.getBounds(coordinates));
                                } catch (Exception e) {
                                }
                            }
                            layerDTO.setBounds(bounds);
                            layerDTO.setCredits("Geocento Ltd partners");
                            layerDTO.setVersion("1.3");
                            layers.add(layerDTO);
                        }
                    }
                    break;
                }
            }
            return layers;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EIException("Could not load layers!");
        } finally {
            em.close();
        }
    }

    @Override
    public void saveImageAlert(String name, Date endDate, String searchRequestId, String orderId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(name == null || name.length() == 0) {
            throw new EIException("Missing name");
        }
        if(endDate == null || endDate.before(new Date())) {
            throw new EIException("Missing or not valid end date");
        }
        // get sensor filter
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            int count = user.getImageAlerts().size();
            if(count > Utils.getSettings().getMaxImageAlerts()) {
                throw new EIException("Maximum number of alerts reached, please unregister some of your alerts first");
            }
            if(ListUtil.findValue(user.getImageAlerts(), value -> {return value.getSearchId().contentEquals(searchRequestId);}) != null) {
                throw new EIException("An alert already exists for this search, please remove alert first");
            }
            Order order = null;
            if(orderId != null) {
                order = em.find(Order.class, orderId);
                if(order == null) {
                    throw new EIException("Order does not exist");
                }
            }
            checkOrderUser(order, user);
            em.getTransaction().begin();
            ImageAlert imageAlert = new ImageAlert();
            imageAlert.setName(name);
            imageAlert.setOwner(user);
            imageAlert.setSearchId(searchRequestId);
            imageAlert.setEndDate(endDate);
            imageAlert.setOrder(order);
            imageAlert.setUpdatePeriod(24);
            user.getImageAlerts().add(imageAlert);
            em.persist(imageAlert);
            em.getTransaction().commit();
            try {
                // send email
                String emailAddress = imageAlert.getOwner().getEmail();
                MailContent mailContent = new MailContent(MailContent.EMAIL_TYPE.CONSUMER);
                mailContent.addTitle("Registration of your image alert '" + name + "'");
                mailContent.addLine("Thank you for registering your image alert with CloudEO. " +
                        "We will scan our archives on a daily basis for any new imagery available in this area. " +
                        "You will be notified by email whenever we have matches.");
                mailContent.addLine("Important! If you wish to unregister your image alert, please click on this link <a href='" +
                        UtilImageAlert.generateUnregisterLink(imageAlert.getId(), emailAddress) + "'>unregister alert</a>.");
                mailContent.sendEmail(user, "Your image alert registration with Application", Utils.getSettings().getOrderEmail());
            } catch (Exception e) {
                logger.error("Failed to send email, reason is " + e.getMessage());
            }
        } catch (Exception e) {
            if(e instanceof EIException) {
                throw (EIException) e;
            }
            throw new EIException("Could not save your alert, please try again");
        } finally {
            em.close();
        }
    }

    @Override
    public List<WorkspaceSummaryDTO> loadWorkspaces(String name, Date start, Date stop) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            List<String> statements = new ArrayList<String>();
            boolean hasStart = start != null;
            boolean hasStop = stop != null;
            if(hasStart) {
                if (start.after(new Date())) {
                    throw new EIException("Start date cannot after today");
                }
                if (hasStop && start.after(stop)) {
                    throw new EIException("Start date cannot be after end date");
                }
                statements.add(hasStop ? "w.creationDate between :start and :stop" :
                        "w.creationDate > :start "
                );
            } else if(hasStop) {
                statements.add("w.creationDate < :stop ");
            }
            boolean hasName = !StringUtils.isEmpty(name);
            if(hasName) {
                statements.add("lower(w.name) like '%" + name.toLowerCase() + "%' ");
            }
            User user = em.find(User.class, logUserName);
            boolean isAdministrator = user.getUserRole() == USER_ROLE.ADMINISTRATOR;
            if(!isAdministrator) {
                statements.add("w.owner = :owner");
            }

            boolean hasStatements = statements.size() > 0;
            String queryString = "select w from Workspace w " +
                    (hasStatements ? ("where " + StringUtils.join(statements, " and ")) : "") +
                    " order by w.creationDate DESC ";

            TypedQuery<Workspace> query = em.createQuery(queryString, Workspace.class);
            if(hasStart) {
                query.setParameter("start", start);
            }
            if(hasStop) {
                query.setParameter("stop", stop);
            }
            if(!isAdministrator) {
                query.setParameter("owner", user);
            }

            return ListUtil.mutate(query.getResultList(), (ListUtil.Mutate<Workspace, WorkspaceSummaryDTO>) workspace -> {
                WorkspaceSummaryDTO workspaceSummaryDTO = new WorkspaceSummaryDTO();
                workspaceSummaryDTO.setId(workspace.getId());
                workspaceSummaryDTO.setName(workspace.getName());
                workspaceSummaryDTO.setDescription(workspace.getDescription());
                workspaceSummaryDTO.setCreationDate(workspace.getCreationDate());
                workspaceSummaryDTO.setBounds(getBoundFromProductOrders(workspace.getProductOrders()));
                workspaceSummaryDTO.setNumOfProducts(workspace.getProductOrders().size());
                workspaceSummaryDTO.setPublishedProducts(workspace.getPublishProductRequests().size());
                return workspaceSummaryDTO;
            });
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public WorkspaceDTO loadWorkspace(String workspaceId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            Workspace workspace = em.find(Workspace.class, workspaceId);
            checkWorkspaceUser(workspace, user);
            WorkspaceDTO workspaceDTO = new WorkspaceDTO();
            workspaceDTO.setId(workspace.getId());
            workspaceDTO.setName(workspace.getName());
            workspaceDTO.setDescription(workspace.getDescription());
            workspaceDTO.setCreationDate(workspace.getCreationDate());
            workspaceDTO.setProductOrders(ListUtil.mutate(workspace.getProductOrders(), productOrder -> {return convertProductOrderDTO(productOrder);}));
            workspaceDTO.setPublishedProducts(ListUtil.mutate(workspace.getPublishProductRequests(), productOrder -> {return convertPublishProductRequest(productOrder);}));
            workspaceDTO.setPublishedLayers(ListUtil.mutate(workspace.getPublishedLayers(), publishedLayer -> {return convertPublishedLayer(publishedLayer);}));
            return workspaceDTO;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private PublishedLayerDTO convertPublishedLayer(PublishedLayer publishedLayer) {
        PublishedLayerDTO publishedLayerDTO = new PublishedLayerDTO();
        publishedLayerDTO.setId(publishedLayer.getId());
        publishedLayerDTO.setName(publishedLayer.getName());
        publishedLayerDTO.setDescription(publishedLayer.getDescription());
        publishedLayerDTO.setThumbnailUrl(publishedLayer.getThumbnailUrl());
        publishedLayerDTO.setPublishedWorkspace(publishedLayer.getPublishedWorkspace());
        publishedLayerDTO.setPublishedLayer(publishedLayer.getPublishedId());
        publishedLayerDTO.setPublished(publishedLayer.isPublished());
        publishedLayerDTO.setProductLayers(ListUtil.mutate(publishedLayer.getProductLayers(), productLayer -> convertProductLayer(productLayer)));
        publishedLayerDTO.setAdditionalLayers(ListUtil.mutate(publishedLayer.getAdditionalLayers(), externalLayer -> convertExternalLayer(externalLayer)));
        return publishedLayerDTO;
    }

    private ProductLayerDTO convertProductLayer(ProductLayer productLayer) {
        ProductLayerDTO productLayerDTO = new ProductLayerDTO();
        productLayerDTO.setId(productLayer.getId());
        productLayerDTO.setName(productLayer.getName());
        productLayerDTO.setDescription(productLayer.getDescription());
        //productLayerDTO.setThubmnailUrl(productLayer.getTh)
        productLayerDTO.setProductType(productLayer.getProductType());
        productLayerDTO.setPublishRequestId(productLayer.getPublishRequestId());
        productLayerDTO.setProductWMSServiceURL(Utils.getSettings().getProductServiceWMSURL().replace("$userName", productLayer.getWorkspace()));
        productLayerDTO.setPublishUri(productLayer.getPublishUri());
        productLayerDTO.setBounds(productLayer.getBounds());
        productLayerDTO.setDisplayed(productLayer.isDisplayed());
        productLayerDTO.setSldName(productLayer.getSldName());
        return productLayerDTO;
    }

    private ExternalLayerDTO convertExternalLayer(ExternalLayer externalLayer) {
        ExternalLayerDTO externalLayerDTO = new ExternalLayerDTO();
        externalLayerDTO.setId(externalLayer.getId());
        externalLayerDTO.setName(externalLayer.getName());
        externalLayerDTO.setDescription(externalLayer.getDescription());
        externalLayerDTO.setProductType(externalLayer.getProductType());
        externalLayerDTO.setPublishUri(externalLayer.getPublishUri());
        externalLayerDTO.setBounds(externalLayer.getBounds());
        externalLayerDTO.setDisplayed(externalLayer.isDisplayed());
        externalLayerDTO.setSldName(externalLayer.getSldName());
        return externalLayerDTO;
    }

    @Override
    public String createWorkspace(String name, String description) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            // check workspace name is unique
            int count = ListUtil.count(user.getWorkspaces(), value -> {return value.getName().contentEquals(name);});
            if(count > 0) {
                throw new EIException("Name for workspace is already taken");
            }
            em.getTransaction().begin();
            Workspace workspace = new Workspace();
            workspace.setId(keyGenerator.CreateKey());
            workspace.setOwner(user);
            workspace.setName(name);
            workspace.setDescription(description);
            workspace.setCreationDate(new Date());
            user.getWorkspaces().add(workspace);
            em.persist(workspace);
            em.getTransaction().commit();
            return workspace.getId();
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public WorkspaceSummaryDTO addProductWorkspace(String workspaceId, String productId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, logUserName);
            Workspace workspace = em.find(Workspace.class, workspaceId);
            if(workspace == null) {
                throw new EIException("Workspace with id " + workspaceId + " does not exist");
            }
            checkWorkspaceUser(workspace, user);
            ProductOrder productOrder = em.find(ProductOrder.class, productId);
            if(productOrder == null) {
                throw new EIException("Product order with id " + productId + " does not exist");
            }
            workspace.getProductOrders().add(productOrder);
            productOrder.getWorkspaces().add(workspace);
            em.getTransaction().commit();
            WorkspaceSummaryDTO workspaceSummaryDTO = new WorkspaceSummaryDTO();
            workspaceSummaryDTO.setId(workspace.getId());
            workspaceSummaryDTO.setName(workspace.getName());
            return workspaceSummaryDTO;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private void checkWorkspaceUser(Workspace workspace, User dbUser) throws EIException {
        if(workspace == null) {
            throw new EIException("Unknown workspace");
        }
        if(dbUser.getUserRole() != USER_ROLE.ADMINISTRATOR && !workspace.getOwner().getUsername().contentEquals(dbUser.getUsername())) {
            throw new EIException("Access not allowed");
        }
    }

    @Override
    public List<ProcessDTO> loadProcesses(String workspaceId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            Workspace workspace = em.find(Workspace.class, workspaceId);
            if(workspace == null) {
                throw new EIException("Workspace with id " + workspaceId + " does not exist");
            }
            List<ProductOrder> productOrders = workspace.getProductOrders();
            HashSet<String> plaformNames = new HashSet<String>();
            for(ProductOrder productOrder : productOrders) {
                String platformName = PublishAPIUtils.getPlatformName(productOrder.getProductRequest().getSatelliteName());
                plaformNames.add(platformName + "_(.*)");
            }
            List<PublishProductProcessDTO> processes = ProductPublisherAPIUtil.getProcesses(0, 100,
                    null,
                    StringUtils.join(new ArrayList<String>(plaformNames), ","),
                    null,
                    null);
            return ListUtil.mutate(processes, new ListUtil.Mutate<PublishProductProcessDTO, ProcessDTO>() {
                @Override
                public ProcessDTO mutate(PublishProductProcessDTO publishProductProcessDTO) {
                    ProcessDTO processDTO = new ProcessDTO();
                    processDTO.setId(publishProductProcessDTO.getId());
                    processDTO.setName(publishProductProcessDTO.getName());
                    processDTO.setDescription(publishProductProcessDTO.getDescription());
                    processDTO.setSupportedPlatformProducts(ListUtil.toString(publishProductProcessDTO.getSupportedPlatformProducts(), new ListUtil.GetLabel<PlatformDTO>() {
                        @Override
                        public String getLabel(PlatformDTO platformDTO) {
                            return platformDTO.getName() + " (" +
                                    ListUtil.toString(platformDTO.getProductTypes(), productTypeDTO -> productTypeDTO.getName(), ", ") + ")";
                        }
                    }, ", "));
                    return processDTO;
                }
            });
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<ProcessDTO> loadProductProcesses(String productId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            ProductOrder productOrder = em.find(ProductOrder.class, productId);
            if(productOrder == null) {
                throw new EIException("Product order with id " + productId + " does not exist");
            }
            String platformName = PublishAPIUtils.getPlatformName(productOrder.getProductRequest().getSatelliteName());
            platformName = "^" + platformName + "_(.*)";
            List<PublishProductProcessDTO> processes = ProductPublisherAPIUtil.getProcesses(0, 100,
                    null,
                    platformName,
                    null,
                    null);
            return ListUtil.mutate(processes, new ListUtil.Mutate<PublishProductProcessDTO, ProcessDTO>() {
                @Override
                public ProcessDTO mutate(PublishProductProcessDTO publishProductProcessDTO) {
                    ProcessDTO processDTO = new ProcessDTO();
                    processDTO.setId(publishProductProcessDTO.getId());
                    processDTO.setName(publishProductProcessDTO.getName());
                    processDTO.setDescription(publishProductProcessDTO.getDescription());
                    processDTO.setSupportedPlatformProducts(ListUtil.toString(publishProductProcessDTO.getSupportedPlatformProducts(), new ListUtil.GetLabel<PlatformDTO>() {
                        @Override
                        public String getLabel(PlatformDTO platformDTO) {
                            return platformDTO.getName() + " (" +
                                    ListUtil.toString(platformDTO.getProductTypes(), productTypeDTO -> productTypeDTO.getName(), ", ") + ")";
                        }
                    }, ", "));
                    processDTO.setParameters(ListUtil.mutate(publishProductProcessDTO.getParameters(), new ListUtil.Mutate<ParameterDTO, Property>() {
                        @Override
                        public Property mutate(ParameterDTO parameterDTO) {
                            Property property = null;
                            switch (parameterDTO.getParameterType()) {
                                case intValue:
                                    property = new IntegerProperty(parameterDTO.getName(), parameterDTO.getDescription(), null, true, 0, 100000);
                                    break;
                                case doubleValue:
                                    property = new DoubleProperty(parameterDTO.getName(), parameterDTO.getDescription(), null, true, 0, 100000.0);
                                    break;
                                case choiceValue:
                                    String[] options = parameterDTO.getOptions().split(",");
                                    property = new ChoiceProperty(parameterDTO.getName(), parameterDTO.getDescription(), null, false, true, options);
                                    break;
                            }
                            return property;
                        }
                    }));
                    return processDTO;
                }
            });
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public PublishProductProcessResponse runProcess(String name, String description, String workspaceId, Long processId, List<String> productOrderIds, List<Property> parameters) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

        PublishProductProcessResponse publishProductProcessResponse = new PublishProductProcessResponse();

        EntityManager em = EMF.get().createEntityManager();
        try {
            String errorMessage = "";
            List<PublishedProductDTO> publishedProducts = new ArrayList<PublishedProductDTO>();
            // we need to do a query each time because of the rollback
            // when rollback is done the entities are not synchronised any more and need reloading
            for(String productOrderId : productOrderIds) {
                try {
                    em.getTransaction().begin();
                    Workspace workspace = em.find(Workspace.class, workspaceId);
                    ProductOrder productOrder = ListUtil.findValue(workspace.getProductOrders(),
                            value -> {
                                return value.getId().contentEquals(productOrderId);
                            });
                    // work out parameter values from properties
                    List<ParameterValue> parameterValues = new ArrayList<ParameterValue>();
                    for(Property property : parameters) {
                        if(property == null || property.getValue() == null) {
                            continue;
                        }
                        ParameterValue parameterValue = new ParameterValue();
                        parameterValue.setName(property.getName());
                        if (property instanceof IntegerProperty) {
                            parameterValue.setIntValue(((IntegerProperty) property).getValue());
                        } else if(property instanceof DoubleProperty) {
                            parameterValue.setDoubleValue(((DoubleProperty) property).getValue());
                        } else if(property instanceof ChoiceProperty) {
                            parameterValue.setOptionValue(((ChoiceProperty) property).getValue());
                        } else {
                            continue;
                        }
                        parameterValues.add(parameterValue);
                    }
                    // store request
                    ProductPublishRequest productPublishRequest = new ProductPublishRequest();
                    productPublishRequest.setCreated(new Date());
                    productPublishRequest.setStatus(PUBLICATION_STATUS.Requested);
                    productPublishRequest.setName("Process '" + name + "' on product #" + productOrder.getId());
                    productPublishRequest.setDescription("Process '" + name + "' on product '" + productOrder.getLabel() + "'. " +
                            "Process description '" + productOrder.getDescription() + "'" +
                            (parameterValues.size() > 0 ?
                                    (", with parameters " + ListUtil.toString(parameterValues, value -> value.getName() + " = " + value.getIntValue() != null ? value.getIntValue().toString() : value.getDoubleValue() != null ? value.getDoubleValue().toString() : value.getOptionValue(), ", ")) : "")
                    );
                    productPublishRequest.setProductOrder(productOrder);
                    em.persist(productPublishRequest);
                    // now run the process
                    PublishProductRequest publishProductRequest = new PublishProductRequest();
                    publishProductRequest.setProcessId(processId);
                    String satelliteName = productOrder.getProductRequest().getSatelliteName();
                    String platformName = PublishAPIUtils.getPlatformName(satelliteName);
                    publishProductRequest.setPlatformName(platformName);
                    // TODO - find a way to get the product type
                    MethodValues.PublishMethod method = PublishAPIUtils.getPublishMethodValue(satelliteName);
                    publishProductRequest.setProductType(method.productType);
                    publishProductRequest.setAoi(productOrder.getSelectionGeometry());
                    // add parameters
                    publishProductRequest.setParameterValues(parameterValues);
                    // add publishing information
                    publishProductRequest.setWorkspace(userName);
                    publishProductRequest.setLayerName(workspaceId + "_" + productOrder.getId() + "_" + productPublishRequest.getId());
                    publishProductRequest.setCallbackUrl(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.callBackUrlProductPublisherWorkspace).replace("$id", workspaceId));
                    PublishProductRequestResponse productRequestResponse = null;
                    // TODO - if not a local publisher it should go into the state machine...
                    if (ProductPublisherAPIUtil.isPublisherLocal()) {
                        publishProductRequest.setFilePath(productOrder.getFileLocation());
                        productRequestResponse = ProductPublisherAPIUtil.createTask(publishProductRequest);
                    } else {
                        productRequestResponse = ProductPublisherAPIUtil.createTask(new File(productOrder.getFileLocation()), publishProductRequest);
                    }
                    if (productRequestResponse.getCode() < 300) {
                        productPublishRequest.setPublishTaskId(productRequestResponse.getTaskId());
                        productPublishRequest.setStatus(PUBLICATION_STATUS.Publishing);
                    } else {
                        productPublishRequest.setStatus(PUBLICATION_STATUS.Failed);
                        productPublishRequest.setStatusMessage(productRequestResponse.getMessage());
                    }
                    workspace.getPublishProductRequests().add(productPublishRequest);
                    em.getTransaction().commit();
                    // add the published product to the response
                    publishedProducts.add(convertPublishProductRequest(productPublishRequest));
                } catch (Exception e) {
                    if(em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    logger.error(e.getMessage(), e);
                    errorMessage += "Could not process product order #" + productOrderId + ", reason is " + e.getMessage() + ". ";
                }
            }
            publishProductProcessResponse.setMessage(errorMessage);
            publishProductProcessResponse.setPublishedProducts(publishedProducts);
            return publishProductProcessResponse;
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public PublishedLayerDTO savePublishedLayer(PublishedLayerDTO publishedLayerDTO) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            PublishedLayer publishedLayer = em.find(PublishedLayer.class, publishedLayerDTO.getId());
            publishedLayer.setName(publishedLayerDTO.getName());
            publishedLayer.setDescription(publishedLayerDTO.getDescription());
            publishedLayer.setThumbnailUrl("./img/notPublished.png");
            // check the difference between the product layers
            // first check if some layers have been removed
            // we need to do this first otherwise we will filter the newly created ones
            publishedLayer.setProductLayers(ListUtil.filterValues(publishedLayer.getProductLayers(),
                    // check if layer is still in the list of layers
                    productLayer -> {return ListUtil.findValue(publishedLayerDTO.getProductLayers(),
                            // look for correspondance
                            productLayerDTO -> {return productLayerDTO.getId() != null && productLayerDTO.getId().contentEquals(productLayer.getId());}) != null;}));
            // now look for the new ones
            for(ProductLayerDTO productLayerDTO : publishedLayerDTO.getProductLayers()) {
                ProductLayer productLayer = null;
                if(productLayerDTO.getId() == null) {
                    // create new product layer
                    productLayer = new ProductLayer();
                    productLayer.setId(keyGenerator.CreateKey());
                    productLayer.setPublishedLayer(publishedLayer);
                    publishedLayer.getProductLayers().add(productLayer);
                    em.persist(productLayer);
                } else {
                    productLayer = ListUtil.findValue(publishedLayer.getProductLayers(), value -> {return value.getId().contentEquals(productLayerDTO.getId());});
                    if(productLayer == null) {
                        throw new EIException("Product layer does not exist");
                    }
                }
                productLayer.setName(productLayerDTO.getName());
                productLayer.setDescription(productLayerDTO.getDescription());
                // TODO - change somehow
                productLayer.setWorkspace(userName);
                productLayer.setPublishRequestId(productLayerDTO.getPublishRequestId());
                productLayer.setProductType(productLayerDTO.getProductType());
                productLayer.setPublishUri(productLayerDTO.getPublishUri());
                productLayer.setBounds(productLayerDTO.getBounds());
                productLayer.setDisplayed(productLayerDTO.isDisplayed());
                productLayer.setSldName(productLayerDTO.getSldName());
            }
            // needs publishing
            publishedLayer.setPublished(false);
            em.getTransaction().commit();
            return convertPublishedLayer(publishedLayer);
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void removePublishedLayer(String layerId) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, userName);
            em.getTransaction().begin();
            PublishedLayer publishedLayer = em.find(PublishedLayer.class, layerId);
            if(publishedLayer == null) {
                throw new EIException("Layer does not exist");
            }
            Workspace workspace = publishedLayer.getWorkspace();
            checkWorkspaceUser(workspace, user);
            // unpublish layer
            ProductPublisherAPIUtil.removeGroupLayers(publishedLayer.getPublishedWorkspace(), publishedLayer.getPublishedId());
            // remove all product layers first
            for(ProductLayer productLayer : publishedLayer.getProductLayers()) {
                productLayer.setPublishedLayer(null);
                em.remove(productLayer);
            }
            workspace.getPublishedLayers().remove(publishedLayer);
            publishedLayer.setWorkspace(null);
            // product layers and additional layers should be removed through cascade settings
            em.remove(publishedLayer);
            em.getTransaction().commit();
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public PublishedLayerDTO publishPublishedLayer(String layerId) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            PublishedLayer publishedLayer = em.find(PublishedLayer.class, layerId);
            // for now the product publisher can only handle all layers within the same workspace
            String workspace = publishedLayer.getPublishedWorkspace();
            if(workspace == null) {
                for (ProductLayer productLayer : publishedLayer.getProductLayers()) {
                    if (workspace == null) {
                        workspace = productLayer.getWorkspace();
                    } else {
                        if (!workspace.contentEquals(productLayer.getWorkspace())) {
                            throw new EIException("All layers need to be in the same workspace");
                        }
                    }
                }
                publishedLayer.setPublishedWorkspace(workspace);
            }
            String publishedId = publishedLayer.getPublishedId();
            if(publishedId == null) {
                publishedId = ProductPublisherAPIUtil.createGroupLayers(workspace,
                        publishedLayer.getDescription(),
                        publishedLayer.getProductLayers());
                publishedLayer.setPublishedId(publishedId);
            } else {
                ProductPublisherAPIUtil.updateGroupLayers(workspace,
                        publishedLayer.getPublishedId(),
                        publishedLayer.getDescription(),
                        publishedLayer.getProductLayers());
            }
            // update the thumbnail
            publishedLayer.setThumbnailUrl(ProductPublisherAPIUtil.getLayerThumbnail(publishedLayer.getPublishedWorkspace(),
                    publishedLayer.getPublishedId(),
                    200, 200));
            publishedLayer.setPublished(true);
            em.getTransaction().commit();
            return convertPublishedLayer(publishedLayer);
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public String uploadOrder(String orderId) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

/*
        EntityManager em = EMF.get().createEntityManager();
        try {
            Order order = em.find(Order.class, orderId);
            if(order == null) {
                throw new EIException("Could not find order");
            }
            // get the directory to write to
            String orderServerPath = Utils.getSettings().getOrderServerPath();
            File directory = new File(orderServerPath, orderId);
            if(!directory.exists()) {
                directory.mkdirs();
            }
            int numberOfFiles = 0;
            long totalFilesSize = 0;
            // copy all images across
            for(ProductOrder productOrder : order.getProductOrders()) {
                if(productOrder.getStatus() == PRODUCTORDER_STATUS.Completed && !StringUtils.isEmpty(productOrder.getFileLocation())) {
                    File productOrderFile = new File(productOrder.getFileLocation());
                    // TODO - check file already exists?
                    // File copyFile = new File(directory, )
                    if (productOrderFile.exists()) {
                        FileUtils.copyFileToDirectory(productOrderFile, directory);
                    }
                    numberOfFiles++;
                    totalFilesSize += productOrderFile.length();
                }
            }
            return "Copied " + numberOfFiles + " files for a total of " + (totalFilesSize / 1024 / 1024) + " MB of data";
        } catch(Exception e) {
            handleException(em, e);
            throw new EIException("Server error.");
        } finally {
            em.close();
        }
*/
        return null;
    }

    @Override
    public void republishProductOrder(String productOrderId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(productOrderId == null) {
            throw new EIException("Product order id missing");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            if(productOrder == null) {
                throw new EIException("Could not find product with id " + productOrderId);
            }

            if(productOrder.getStatus() != PRODUCTORDER_STATUS.Completed) {
                throw new EIException("Product order has not been completed yet");
            }
            if(productOrder.getPublicationStatus() != PUBLICATION_STATUS.Published &&
                    productOrder.getPublicationStatus() != PUBLICATION_STATUS.Failed) {
                throw new EIException("Product order publishing has not been completed yet");
            }

            em.getTransaction().begin();

            if( productOrder.getPublishProductRequests() != null) {
                for (ProductPublishRequest publishProductRequest : productOrder.getPublishProductRequests()) {
                    em.remove(publishProductRequest);
                }
                productOrder.getPublishProductRequests().clear();
            }
            productOrder.setPublicationStatus(null);
            // find the initial fetch task
            TypedQuery<ProductFetchTask> query = em.createQuery("select p from ProductFetchTask p where p.productOrder = :productOrder", ProductFetchTask.class);
            query.setParameter("productOrder", productOrder);
            ProductFetchTask productFetchTask = query.getSingleResult();
            productFetchTask.setStatus(STATUS.downloaded);
            productFetchTask.setCompleted(null);
            productFetchTask.setAttempt(0);
            productFetchTask.setStatusMessage(null);
            productFetchTask.setFetchDate(new Date());
            try {
                logger.debug("Deleting product publishing with id " + productFetchTask.getPublishTaskId());
                ProductPublisherAPIUtil.deleteProduct(productFetchTask.getPublishTaskId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            productFetchTask.setPublishTaskId(null);
            em.getTransaction().commit();
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public SamplesResult loadSamples(SensorFilters sensorFilters, List<Long> instrumentIds, String keyword, int start, int limit) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        SamplesResult samplesResult = new SamplesResult();
        // TODO - call EarthImages API to retrieve all samples?
        EntityManager em = EMF.get().createEntityManager();
        try {
            List<String> additionalStatements = new ArrayList<String>();
            // call EarthImages to retrieve the list of instrument ids matching the sensor filter
            boolean hasInstruments = instrumentIds != null && instrumentIds.size() > 0;
            boolean hasSensorFilter = sensorFilters != null;
            boolean filterOnSensors = hasInstruments || hasSensorFilter;
            if(!hasInstruments) {
                if(hasSensorFilter) {
                    List<Satellite> satellites = EIAPIUtil.listSatellites(sensorFilters);
                    instrumentIds = new ArrayList<Long>();
                    for (Satellite satellite : satellites) {
                        instrumentIds.addAll(ListUtil.mutate(satellite.getInstruments(), value -> value.getId()));
                    }
                }
            }
            if(filterOnSensors && instrumentIds.size() == 0) {
                samplesResult.setSamples(new ArrayList<SampleDTO>());
                return samplesResult;
            }
            if(filterOnSensors) {
                additionalStatements.add("instrumentid IN (" + ListUtil.toString(instrumentIds, value -> value.toString(), ",") + ")");
            }
            boolean filterOnKeywords = !StringUtils.isEmpty(keyword);
            if(filterOnKeywords) {
                keyword = generateKeywords(keyword);
                additionalStatements.add("tsv @@ keywords");
            }
/*
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<ProductSample> cq = cb.createQuery(ProductSample.class);
            Root<ProductSample> root = cq.from(ProductSample.class);
            ListJoin<ProductSample, String> phoneNumbers = root.join(ProductSample_.phoneNumbers);

            ParameterExpression<String> paramPhoneNumber = cb.parameter(String.class);
            cq.where(cb.like(phoneNumbers, paramPhoneNumber));
*/

            Query countQuery = em.createNativeQuery("select count(id) from productsample_keywords_view s" +
                    (filterOnKeywords ? ", to_tsquery('" + keyword + "') as keywords" : "") +
                    (additionalStatements.size() > 0 ? (" where " + StringUtils.join(additionalStatements, " AND ")) : ""));
            // TODO - add order by rank
            // select ts_rank(tsv, keywords, 1) AS rank from productsample_keywords_view s, to_tsquery('lan:*') as keywords where tsv @@ keywords
            Query query = em.createNativeQuery("select id" +
                            (filterOnKeywords ? ", ts_rank(tsv, keywords, 1) AS rank" : "") +
                    " from productsample_keywords_view s" +
                    (filterOnKeywords ? ", to_tsquery('" + keyword + "') as keywords" : "") +
                    (additionalStatements.size() > 0 ? (" where " + StringUtils.join(additionalStatements, " AND ")) : "") +
                    (filterOnKeywords ? " order by rank desc" : ""));
            // first count total number of results
            Long count = (Long) countQuery.getSingleResult();
            samplesResult.setTotalResults(count.intValue());
            // now get results based on start and limit
            query.setFirstResult(start);
            query.setMaxResults(limit);
            samplesResult.setSamples(ListUtil.mutate(query.getResultList(), value -> {
                String sampleId = filterOnKeywords ? (String) ((Object[]) value)[0] : (String) value;
                ProductSample productSample = em.find(ProductSample.class, sampleId);
                SampleDTO sampleDTO = new SampleDTO();
                sampleDTO.setId(productSample.getId());
                sampleDTO.setName(productSample.getTitle());
                sampleDTO.setDescription(productSample.getDescription());
                sampleDTO.setInstrumentId(productSample.getInstrumentId());
                sampleDTO.setKeywords(new ArrayList<String>(productSample.getKeywords()));
                ProductOrderDTO productOrder = convertProductOrderDTO(productSample.getProductOrder());
                sampleDTO.setProductOrderId(productOrder.getId());
                sampleDTO.setOriginalProductId(productOrder.getOriginalProductId());
                sampleDTO.setPlatform(productSample.getProductOrder().getProductRequest().getSatelliteName() + " - " + productSample.getProductOrder().getProductRequest().getSensorName());
                sampleDTO.setCoordinates(ListUtil.toList(productOrder.getCoordinates()));
                sampleDTO.setProductMetadataDTO(productOrder.getPublishedProducts().get(0));
                sampleDTO.setProductWMSServiceURL(productOrder.getProductWMSServiceURL());
                sampleDTO.setProductFileName(productOrder.getProductFileName());
                sampleDTO.setProductFileSizeBytes(productOrder.getProductFileSizeBytes());
                try {
                    sampleDTO.setDownloadManualURL(ServerUtil.getServiceUrl("api/download-product/download/" + productOrder.getPolicyId() + "/manual/"));
                } catch (MalformedURLException e) {
                    logger.error(e.getMessage(), e);
                }
                sampleDTO.setThumbnail(productOrder.getThumbnailURL());
                return sampleDTO;
            }));
            return samplesResult;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    public static String generateKeywords(String textFilter) {
        // check if last character is a space
        boolean partialMatch = !textFilter.endsWith(" ");
        textFilter.trim();
        // break down text into sub words
        String[] words = textFilter.split("\\s+");
        String keywords = StringUtils.join(words, " | ");
        if (partialMatch) {
            keywords += ":*";
        }
        return keywords;
    }

    @Override
    public List<String> fetchKeywords(String keyword) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        // TODO - call EarthImages API to retrieve all samples?
        EntityManager em = EMF.get().createEntityManager();
        try {
            return em.createNativeQuery("select distinct keywords from productsample_keywords " +
                    (StringUtils.isEmpty(keyword) ? "" : "where lower(keywords) LIKE '%" + keyword.toLowerCase() + "%' ") +
                    "order by keywords desc;").getResultList();
            //return ListUtil.toList(new String[]{"farming", "city", "stereo"});
        } catch (Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public UserLicenseInformation loadUserLicenseInformation() throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        // TODO - call EarthImages API to retrieve all samples?
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            UserLicenseInformation userLicenseInformation = new UserLicenseInformation();
            userLicenseInformation.setUserName(user.getFirstName() + " " + user.getLastName());
            userLicenseInformation.setOrganisation(user.getCompany());
            // TODO - change file path to a URL
            userLicenseInformation.setSignatureUrls(ListUtil.mutate(user.getSignatures(), signature -> signature.getPath()));
            return userLicenseInformation;
        } catch (Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public String signEULA(String eiOrderId, Long eulaDocumentId, String userName, String organisation, String pngImageData) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            // get all related elements first
            User user = em.find(User.class, logUserName);
            // save signature in the image folder
            File signatureFile = new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.baseThumbnailsDirectory), "signature_" + keyGenerator.CreateKey() + ".png");
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(pngImageData.replace("data:image/png;base64,", ""))));
            ImageIO.write(img, "png", signatureFile);
            File signedPDF = ServerUtil.getUserDocumentFile(logUserName, "signedpdf_" + keyGenerator.CreateKey() + ".pdf");
            // TODO - have the ability to load and/or save
            Signature signature = new Signature();
            signature.setId(keyGenerator.CreateKey());
            signature.setOwner(user);
            signature.setPath(signatureFile.getAbsolutePath());
            signature.setCreationTime(new Date());
            em.persist(signature);
            user.getSignatures().add(signature);
            // now send sign license request and expect product order to be updated
            SignLicenseRequest signLicenseRequest = new SignLicenseRequest();
            signLicenseRequest.setOrderId(eiOrderId);
            signLicenseRequest.setEulaDocumentId(eulaDocumentId);
            signLicenseRequest.setUserId(logUserName);
            signLicenseRequest.setUserName(userName);
            signLicenseRequest.setOrganisation(organisation);
            signLicenseRequest.setPngImageData(pngImageData);
            SignedLicenseDTO signedLicenseDTO = EIAPIUtil.signEULA(signLicenseRequest);
            // save the license locally
            String signedLicenseUrlValue = signedLicenseDTO.getDownloadUrl();
            File eulaPDFFile = ServerUtil.getUserDocumentFile(logUserName, signedLicenseUrlValue.substring(signedLicenseUrlValue.lastIndexOf("/")) + ".pdf");
            EIAPIUtil.downloadFile(eulaPDFFile, signedLicenseUrlValue);
            //FileUtils.copyURLToFile(new URL(signedLicenseUrlValue), eulaPDFFile);
            SignedLicense signedLicense = new SignedLicense();
            signedLicense.setId(keyGenerator.CreateKey());
            signedLicense.setOwner(user);
            signedLicense.setPolicyId(eulaDocumentId);
            signedLicense.setPath(eulaPDFFile.getAbsolutePath());
            signedLicense.setCreationTime(new Date());
            em.persist(signedLicense);
            // special case if local automated orders as they are handled locally
            // list of product orders to add the signed license
            List<ProductOrder> productOrdersLicenseRequired = null;
            // add license to order if order is provided
            boolean hasOrder = eiOrderId != null;
            if(hasOrder){
                TypedQuery<Order> query = em.createQuery("select o from orders o where :eiOrderId member of o.EIOrderIds", Order.class);
                query.setParameter("eiOrderId", eiOrderId);
                Order order = query.getSingleResult();
                if(order == null) {
                    throw new EIException("Order does not exist with EI Order ID #" + eiOrderId);
                }
                if(!order.getOwner().getUsername().contentEquals(logUserName)) {
                    throw new EIException("Not allowed");
                }
                productOrdersLicenseRequired = ListUtil.filterValues(order.getProductOrders(), value -> {
                    return value.getStatus() == PRODUCTORDER_STATUS.Documentation;
                });
                order.getSignedLicenses().add(signedLicense);
            } else {
                // if no order id is provided it means it is a general license
                // search for all product orders with a status of documentation required
                TypedQuery<ProductOrder> query = em.createQuery("select p from ProductOrder p where p.order.owner.username = :user and p.status = :status", ProductOrder.class);
                query.setParameter("user", logUserName);
                query.setParameter("status", PRODUCTORDER_STATUS.Documentation);
                productOrdersLicenseRequired = query.getResultList();
            }
            // we have product orders with a status of documentation required
            // now look for the product order which match the eula document id selected
            if(productOrdersLicenseRequired != null && productOrdersLicenseRequired.size() > 0) {
                logger.info("Total number of product orders with license to sign is " + productOrdersLicenseRequired.size());
                // retrieve all product policies first
                Set<Long> orderingPolicyIds = ListUtil.mutate(new HashSet<ProductOrder>(productOrdersLicenseRequired), productOrder -> productOrder.getPolicyId());
                HashMap<Long, ProductPolicy> policies = new HashMap<Long, ProductPolicy>();
                for(Long policyId : orderingPolicyIds) {
                    policies.put(policyId, EIAPIUtil.getPolicy(policyId));
                }
                List<ProductOrder> signedLicenseProductOrders = ListUtil.filterValues(productOrdersLicenseRequired, value -> {
                    EULADocumentDTO eulaDocumentDTO = PolicyHelper.getLicensingEULADocument(policies.get(value.getPolicyId()).getLicensingPolicy(), value.getLicenseOption().getPropertyValue());
                    return eulaDocumentDTO != null && eulaDocumentDTO.getId().equals(eulaDocumentId);
                });
                logger.info("Found " + signedLicenseProductOrders.size() + " product orders for signing");
                if(signedLicenseProductOrders.size() > 0) {
                    for (ProductOrder productOrder : signedLicenseProductOrders) {
                        if(isAutomatedOrder(productOrder) && productOrder.getOfferedPrice() != null) {
                            productOrder.setStatus(PRODUCTORDER_STATUS.Quoted);
                        } else {
                            productOrder.setStatus(PRODUCTORDER_STATUS.DocumentationProvided);
                        }
                        try {
                            NotificationSocket.notifyProductOrderStatusChanged(productOrder);
                        } catch (Exception e) {
                            logger.error("Failed to send notification " + e.getMessage(), e);
                        }
                    }
                }
            }
            em.getTransaction().commit();
            return ServerUtil.getServiceUrl("api/license/signed/download/" + signedLicense.getId());
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private boolean isAutomatedOrder(ProductOrder productOrder) {
        return productOrder.getProductRequest() != null && PublishAPIUtils.isPlanetOrdered(productOrder.getProductRequest().getSatelliteName());
    }

    @Override
    public List<FileDTO> loadFileList(String productOrderId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();

        try {
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            if(productOrder == null) {
                throw new WebApplicationException("Product order is not available", Response.Status.BAD_REQUEST);
            }

            if(!ServerUtil.isUserAdministrator(getThreadLocalRequest()) && !productOrder.getOrder().getOwner().getUsername().contentEquals(logUserName)) {
                throw new EIException("Not allowed");
            }

            // get product
            String productPath = productOrder.getFileLocation();

            HashMap<String, FileDTO> files = new HashMap<String, FileDTO>();
            // add root directory
            {
                FileDTO directoryFileDTO = new FileDTO();
                directoryFileDTO.setPath("/");
                directoryFileDTO.setName("");
                directoryFileDTO.setFiles(new ArrayList<FileDTO>());
                files.put("/", directoryFileDTO);
                logger.info("Added root directory");
            }

            final ZipFile file = new ZipFile(productPath);
            try {
                final Enumeration<? extends ZipEntry> entries = file.entries();
                while ( entries.hasMoreElements() ) {
                    final ZipEntry zipEntry = entries.nextElement();
                    String fileName = zipEntry.getName();
                    String parent = new File(fileName).getParent();
                    String directoryPath = zipEntry.isDirectory() ? fileName : parent == null ? "" : parent;
                    directoryPath = sanitizePath(directoryPath);
                    logger.info("Processing entry " + fileName);
                    // look for matching directory
                    FileDTO directoryFileDTO = files.get(directoryPath);
                    if(directoryFileDTO == null) {
                        logger.info("Directory " + directoryPath + " not available");
                        // scan all path and generate folders
                        String filePath = "";
                        FileDTO parentDirectory = null;
                        for(String subdirectory : directoryPath.split("/")) {
                            logger.info("Check subdirectory " + subdirectory + " is available");
                            String currentDirectory = sanitizePath(filePath + "/" + subdirectory);
                            if(!files.containsKey(currentDirectory)) {
                                logger.info("Add subdirectory " + subdirectory);
                                directoryFileDTO = new FileDTO();
                                directoryFileDTO.setPath(currentDirectory);
                                directoryFileDTO.setName(subdirectory);
                                directoryFileDTO.setFiles(new ArrayList<FileDTO>());
                                // add it to the parent directory
                                if(parentDirectory != null) {
                                    parentDirectory.getFiles().add(directoryFileDTO);
                                }
                                files.put(currentDirectory, directoryFileDTO);
                                logger.info("Added directory " + directoryFileDTO.getPath());
                            }
                            parentDirectory = files.get(currentDirectory);
                            filePath = currentDirectory;
                        }
                        directoryFileDTO = files.get(directoryPath);
                    }
                    if(!zipEntry.isDirectory()) {
                        FileDTO fileDTO = new FileDTO();
                        fileDTO.setPath(directoryFileDTO.getPath());
                        fileDTO.setName(new File(fileName).getName());
                        fileDTO.setSizeInBytes(zipEntry.getSize());
                        directoryFileDTO.getFiles().add(fileDTO);
                        logger.info("Adding file " + fileDTO.getName() + " to directory " + directoryFileDTO.getPath());
                    }
                }
            } finally {
                file.close();
            }

/*
            HashMap<String, FileDTO> files = new HashMap<String, FileDTO>();
            ZipInputStream zis = new ZipInputStream(new FileInputStream(new File(productPath)));
            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry != null){
                String fileName = zipEntry.getName();
                String parent = new File(fileName).getParent();
                String directoryPath = zipEntry.isDirectory() ? fileName : parent == null ? "" : parent;
                // look for matching directory
                FileDTO directoryFileDTO = files.get(directoryPath);
                FileDTO parentDirectory = null;
                if(directoryFileDTO == null) {
                    // scan all path
                    String filePath = "";
                    for(String subdirectory : directoryPath.split("/")) {
                        String currentDirectory = filePath + "/" + subdirectory;
                        if(!files.containsKey(currentDirectory)) {
                            directoryFileDTO = new FileDTO();
                            directoryFileDTO.setPath(filePath);
                            directoryFileDTO.setName(subdirectory);
                            directoryFileDTO.setFiles(new ArrayList<FileDTO>());
                            // add it to the parent directory
                            if(parentDirectory != null) {
                                parentDirectory.getFiles().add(directoryFileDTO);
                            }
                            files.put(filePath, directoryFileDTO);
                        }
                        parentDirectory = files.get(currentDirectory);
                        filePath = currentDirectory;
                    }
                }
                if(!zipEntry.isDirectory()) {
                    FileDTO fileDTO = new FileDTO();
                    fileDTO.setPath(directoryFileDTO.getPath());
                    fileDTO.setName(new File(fileName).getName());
                    fileDTO.setSizeInBytes(zipEntry.getSize());
                    directoryFileDTO.getFiles().add(fileDTO);
                }
                zipEntry = zis.getNextEntry();
            }
*/

/*
            java.nio.file.Path sourceFolderPath = Paths.get(productPath);
            Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
                    String fullDirectoryPath = attrs.isDirectory() ? file.toString() : file.getParent().toString();
                    String directoryPath = new File(productPath).getParentFile().toURI().relativize(new File(fullDirectoryPath).toURI()).getPath();
                    // look for matching directory
                    FileDTO directoryFileDTO = files.get(directoryPath);
                    FileDTO parentDirectory = null;
                    if(directoryFileDTO == null) {
                        // scan all path
                        String filePath = "";
                        for(String subdirectory : directoryPath.split("/")) {
                            if(!files.containsKey(filePath)) {
                                directoryFileDTO = new FileDTO();
                                directoryFileDTO.setPath(filePath);
                                directoryFileDTO.setName(subdirectory);
                                directoryFileDTO.setFiles(new ArrayList<FileDTO>());
                                // add it to the parent directory
                                if(parentDirectory != null) {
                                    parentDirectory.getFiles().add(directoryFileDTO);
                                }
                                files.put(filePath, directoryFileDTO);
                            }
                            parentDirectory = files.get(filePath);
                            filePath = new File(filePath, subdirectory).getAbsolutePath();
                        }
                    }
                    if(!attrs.isDirectory()) {
                        FileDTO fileDTO = new FileDTO();
                        fileDTO.setPath(directoryFileDTO.getPath());
                        fileDTO.setName(file.getFileName().toString());
                        fileDTO.setSizeInBytes(attrs.size());
                        directoryFileDTO.getFiles().add(fileDTO);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
*/

            return files.get("/").getFiles();
        } catch (Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private String sanitizePath(String directoryPath) {
        directoryPath = directoryPath.replaceAll("\\\\", "/");
        if(directoryPath.endsWith("/")) {
            directoryPath = directoryPath.substring(0, directoryPath.length() - 1);
        }
        if(!directoryPath.startsWith("/")) {
            directoryPath = "/" + directoryPath;
        }
        directoryPath = directoryPath.replaceAll("//", "/");
        return directoryPath;
    }

    @Override
    public AddProductLayersResponse addPublishedProductsToLayers(String workspaceId, List<String> layerIds, List<ProductMetadataDTO> productMetadataDTOs) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            // do not save
/*
            em.getTransaction().begin();
*/
            Workspace workspace = em.find(Workspace.class, workspaceId);
            // get the layers
            List<PublishedLayer> layers = ListUtil.filterValues(workspace.getPublishedLayers(), value -> {return layerIds.contains(value.getId());});
            // add the product layers
            HashMap<String, List<ProductLayerDTO>> layersAdded = new HashMap<String, List<ProductLayerDTO>>();
            for(PublishedLayer publishedLayer : layers) {
                List<ProductLayerDTO> productLayerDTOS = new ArrayList<ProductLayerDTO>();
                for(ProductMetadataDTO productMetadataDTO : productMetadataDTOs) {
                    ProductLayer productLayer = new ProductLayer();
                    //productLayer.setId(keyGenerator.CreateKey());
                    productLayer.setName(productMetadataDTO.getName());
                    productLayer.setDescription(productMetadataDTO.getDescription());
                    // TODO - change somehow
                    productLayer.setWorkspace(userName);
                    productLayer.setPublishRequestId(productMetadataDTO.getPublishRequestId());
                    productLayer.setProductType(productMetadataDTO.getProductType());
                    productLayer.setPublishUri(productMetadataDTO.getPublishUri());
                    productLayer.setBounds(productMetadataDTO.getCoordinatesWKT());
                    productLayer.setDisplayed(true);
                    productLayer.setPublishedLayer(publishedLayer);
                    publishedLayer.getProductLayers().add(productLayer);
                    productLayerDTOS.add(convertProductLayer(productLayer));
                }
                layersAdded.put(publishedLayer.getId(), productLayerDTOS);
            }
/*
            em.getTransaction().commit();
*/
            AddProductLayersResponse layersResponse = new AddProductLayersResponse();
            layersResponse.setLayersAdded(layersAdded);
            return layersResponse;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public PublishedLayerDTO createPublishedLayer(String workspaceId, String name, String description) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Workspace workspace = em.find(Workspace.class, workspaceId);
            // create layer
            PublishedLayer publishedLayer = new PublishedLayer();
            publishedLayer.setId(keyGenerator.CreateKey());
            publishedLayer.setName(name);
            publishedLayer.setDescription(description);
            publishedLayer.setThumbnailUrl("./img/notPublished.png");
            workspace.getPublishedLayers().add(publishedLayer);
            publishedLayer.setWorkspace(workspace);
            em.persist(publishedLayer);
            em.getTransaction().commit();
            return convertPublishedLayer(publishedLayer);
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void removePublishedProductWorkspace(String workspaceId, Long publishProductId) throws EIException {
        String userName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Workspace workspace = em.find(Workspace.class, workspaceId);
            ProductPublishRequest publishProductRequest = ListUtil.findValue(workspace.getPublishProductRequests(), value -> {
                return value.getId().equals(publishProductId);
            });
            if(publishProductRequest == null) {
                throw new EIException("Unknown publish request");
            }
            //if(publishProductRequest.getStatus() == PUBLICATION_STATUS.Published) {
            try {
                ProductPublisherAPIUtil.deleteProduct(publishProductRequest.getPublishTaskId());
            } catch (Exception e) {
                logger.debug(e.getMessage(), e);
            }
            workspace.getPublishProductRequests().remove(publishProductRequest);
            em.remove(publishProductRequest);
            em.getTransaction().commit();
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private PublishedProductDTO convertPublishProductRequest(ProductPublishRequest productPublishRequest) {
        PublishedProductDTO publishedProductDTO = new PublishedProductDTO();
        publishedProductDTO.setId(productPublishRequest.getId());
        publishedProductDTO.setName(productPublishRequest.getName());
        publishedProductDTO.setDescription(productPublishRequest.getDescription());
        publishedProductDTO.setStatus(productPublishRequest.getStatus());
        publishedProductDTO.setProcessedDate(productPublishRequest.getCompleted());
        try {
            if(productPublishRequest.getStatus() == PUBLICATION_STATUS.Published) {
                PublishProcessProducts publishProducts = ProductPublisherAPIUtil.getProductsPublished(productPublishRequest.getPublishTaskId());
                ArrayList<ProductMetadataDTO> products = new ArrayList<ProductMetadataDTO>();
                for (ProductMetadata productMetadata : publishProducts.getGeneratedProductMetadas()) {
                    products.add(PublishAPIUtils.convertProductMetadata(productMetadata, productPublishRequest));
                }
                publishedProductDTO.setProducts(products);
            }
            switch (publishedProductDTO.getStatus()) {
                case Requested:
                case Publishing: {
                    publishedProductDTO.setThumbnailURL("./img/loading.gif");
                } break;
                case Failed: {
                    publishedProductDTO.setThumbnailURL("./img/no-image.png");
                } break;
                case Published: {
                    publishedProductDTO.setThumbnailURL(productPublishRequest.getThumbnailURL());
                } break;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            publishedProductDTO.setStatusMessage("Issue loading products, reason is: " + e.getMessage());
        }
        return publishedProductDTO;
    }

    @Override
    public void removeProductWorkspace(String workspaceId, String productOrderId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        EntityManager em = EMF.get().createEntityManager();
        try {
            em.getTransaction().begin();
            Workspace workspace = em.find(Workspace.class, workspaceId);
            if(workspace == null) {
                throw new EIException("Workspace with id " + workspaceId + " does not exist");
            }
            ProductOrder productOrder = em.find(ProductOrder.class, productOrderId);
            if(productOrder == null) {
                throw new EIException("Product order with id " + productOrderId + " does not exist");
            }
            workspace.getProductOrders().remove(productOrder);
            productOrder.getWorkspaces().remove(workspace);
            em.getTransaction().commit();
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private EIException handleException(EntityManager em, Exception e) {
        if(em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        logger.error(e.getMessage(), e);
        if(e instanceof EIException) {
            return (EIException) e;
        } else {
            return new EIException("Server error");
        }
    }

    private void reportInSlack(Exception e) {
        SlackAPIHelper.sendMessageWebhookNoException(Utils.getSettings().getPrivateSlackHook(), "Error with EI NEO user request: " + e.getMessage());
    }

    @Override
    protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL, String strongName) {
        serializationPolicy = super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
        return serializationPolicy;
    }
}