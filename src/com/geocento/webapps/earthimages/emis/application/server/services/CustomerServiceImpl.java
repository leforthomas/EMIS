package com.geocento.webapps.earthimages.emis.application.server.services;

import com.geocento.webapps.earthimages.emis.admin.server.publishapis.ProductDownloaderAPIUtil;
import com.geocento.webapps.earthimages.emis.admin.server.publishapis.ProductPublisherAPIUtil;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.server.eventsapi.EventsAPIUtil;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.geocento.webapps.earthimages.emis.application.server.utils.PolicyHelper;
import com.geocento.webapps.earthimages.emis.application.server.utils.ProductRequestUtil;
import com.geocento.webapps.earthimages.emis.application.server.websocket.NotificationSocket;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.geocento.webapps.earthimages.emis.common.server.ServerUtil;
import com.geocento.webapps.earthimages.emis.common.server.domain.EventOrder;
import com.geocento.webapps.earthimages.emis.common.server.domain.User;
import com.geocento.webapps.earthimages.emis.common.server.domain.*;
import com.geocento.webapps.earthimages.emis.common.server.mailing.MailContent;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.PublishAPIUtils;
import com.geocento.webapps.earthimages.emis.common.server.utils.*;
import com.geocento.webapps.earthimages.emis.common.server.utils.slack.SlackAPIHelper;
import com.geocento.webapps.earthimages.emis.common.share.*;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.geocento.webapps.earthimages.emis.common.share.utils.RateTable;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.*;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.metaaps.webapps.earthimages.extapi.server.domain.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.orders.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.*;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.vividsolutions.jts.io.WKTWriter;
import com.xero.model.*;
import org.apache.log4j.Logger;
import org.geojson.geometry.Geometry;
import org.geojson.geometry.Point;
import org.geojson.geometry.Polygon;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.File;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
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

    private void addProductsToOrder(EntityManager em, EventOrder eventOrder, List<ProductRequest> catalogueProductRequests, List<TaskingRequest> taskingRequests, List<ProductRequestDTO> products, User user) throws Exception {
        List<ProductOrder> orderedProducts = new ArrayList<ProductOrder>();
        // add catalogue products
        for(ProductRequest productRequest : catalogueProductRequests) {
            // get the matching product request DTO as it has the product information
            ProductRequestDTO productRequestDTO = ListUtil.findValue(products, value -> value.getId().equals(productRequest.getId()));
            // create the product order
            ProductOrder productOrder = OrderHelper.createProductOrder(productRequest);
            productOrder.setOwner(user);
            productOrder.setPolicyId(productRequestDTO.getPolicy().getId());
            productOrder.setParameters(productRequestDTO.getOrderingOptions());
            productOrder.setLicenseOption(productRequestDTO.getLicenseOption());
            productOrder.setThumbnailURL(productRequestDTO.getProduct().getThumbnail());
            productOrder.setTotalPrice(productRequestDTO.getTotalPrice());
            productOrder.setCurrency(productRequestDTO.getCurrency());
            productOrder.setEventOrder(eventOrder);
            em.persist(productOrder);
            orderedProducts.add(productOrder);
            eventOrder.getProductsOrdered().add(productOrder);
        }
        // add tasking products
        for(TaskingRequest taskingRequest : taskingRequests) {
            ProductRequestDTO productRequestDTO = ListUtil.findValue(products, value -> value.getId().equals(taskingRequest.getId()));
            // create the product order
            ProductOrder productOrder = OrderHelper.createProductOrder(taskingRequest);
            productOrder.setPolicyId(productRequestDTO.getPolicy().getId());
            productOrder.setParameters(productRequestDTO.getOrderingOptions());
            productOrder.setLicenseOption(productRequestDTO.getLicenseOption());
            productOrder.setEventOrder(eventOrder);
            productOrder.setTotalPrice(productRequestDTO.getTotalPrice());
            productOrder.setCurrency(productRequestDTO.getCurrency());
            em.persist(productOrder);
            orderedProducts.add(productOrder);
            eventOrder.getProductsOrdered().add(productOrder);
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
            createOrderRequest.setName("Event #" + eventOrder.getId() + " - batch " + new Date().getTime());
            createOrderRequest.setDescription("Application order from EMIS event");
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
            transaction.setEventOrder(eventOrder);
            NotificationSocket.notifyCreditsUpdated(user);
        }
        // update the last updated date
        eventOrder.setLastUpdate(new Date());
    }

    private OrderParameterRequest convertUserOrderParameter(UserOrderParameter userOrderParameter) {
        OrderParameterRequest orderParameterRequest = new OrderParameterRequest();
        orderParameterRequest.setOrderParameterId(userOrderParameter.getOrderParameterId());
        orderParameterRequest.setValue(userOrderParameter.getPropertyValue());
        return orderParameterRequest;
    }

    @Override
    public String addProductsToEvent(String orderId, List<ProductRequestDTO> products) throws EIException {
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
            EventOrder eventOrder = em.find(EventOrder.class, orderId);
            if (eventOrder == null) {
                throw new EIException("Could not find order with id " + orderId);
            }
            addProductsToOrder(em, eventOrder, cart.getProductRequests(), cart.getTaskingRequests(), products, user);
            for(ProductRequest productRequest : cart.getProductRequests()) {
                productRequest.setUserCart(null);
            }
            cart.getProductRequests().clear();
            for(TaskingRequest taskingRequest : cart.getTaskingRequests()) {
                taskingRequest.setUserCart(null);
            }
            cart.getTaskingRequests().clear();
            OrderHelper.updateOrderStatus(eventOrder);
            em.getTransaction().commit();
            return eventOrder.getId();
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
            EventOrder eventOrder = em.find(EventOrder.class, orderId);
            if(eventOrder == null) {
                throw new EIException("Could not find order with ID " + orderId);
            }
            // get the rate table
            RateTable rateTable = ServerUtil.getCurrencyRateTable();
            for(ProductOrder productOrder : products) {
                // make sure they are all from teh same imageSupplierOrder and from this user too
/*
                if(productOrder.getEventOrder().getOwner() != dbUser) {
                    throw new EIException("Not authorized");
                }
*/
                if(productOrder.getEventOrder() != eventOrder) {
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
                    " in event '" + eventOrder.getId() + "'";

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
                eventOrder.setLastUpdate(new Date());
            }
            eventOrder.setLastUpdate(new Date());

            // check payment first
            // check we have enough money
            Price currentCredit = new Price(dbUser.getCredit().getCurrent(), dbUser.getCredit().getCurrency());
            if(currentCredit.getCurrency().contentEquals(totalPrice.getCurrency()) && currentCredit.getValue() > totalPrice.getValue()) {
                currentCredit.setValue(dbUser.getCredit().getCurrent() - totalPrice.getValue());
            } else {
                throw new Exception("Not enough credits left on your account");
            }
            Transaction debitTransaction = TransactionsHelper.addUserTransaction(em, dbUser, TRANSACTION_TYPE.purchase, (-1) * totalPrice.getValue(), totalPrice.getCurrency(), comment, new Date());
            debitTransaction.setEventOrder(eventOrder);

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

    private List<Product> getCatalogueProducts(List<Product> products) {
        return ListUtil.filterValues(products, value -> value.getType() == TYPE.ARCHIVE);
    }

    private List<Product> getFutureProducts(List<Product> products) {
        return ListUtil.filterValues(products, value -> value.getType() == TYPE.TASKING);
    }

    private boolean isGeocentoId(String productId) {
        boolean geocentoIds = productId.length() == 32;
        // deal with special case of Planet
        if(productId.startsWith("PL_")) {
            geocentoIds = true;
        }
        return geocentoIds;
    }

    @Override
    public EventDTO loadEvent(String eventId, String password) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(eventId == null) {
            throw new EIException("Order id missing");
        }
        // look for event in the event API
        EntityManager em = EMF.get().createEntityManager();
        try {
            EventDTO eventDTO = new EventDTO();
            com.geocento.webapps.earthimages.emis.application.server.eventsapi.EventDTO event = EventsAPIUtil.loadEvent(eventId);
            // we have the event start copying values to the DTO
            eventDTO.setId(eventId);
            EventDescription eventDescription = new EventDescription();
            eventDescription.setType(event.getProperties().getType());
            eventDescription.setTitle(event.getProperties().getTitle());
            eventDescription.setCategory(event.getProperties().getCategory());
            eventDescription.setDescription(event.getProperties().getDescription());
            eventDescription.setStartDate(ServerUtil.parseDate(event.getProperties().getStartDate()));
            eventDescription.setEndDate(ServerUtil.parseDate(event.getProperties().getEndDate()));
            // get the AoI
            AOI aoi = convertGeoJSONToAOI(event.getGeometry());
            eventDescription.setAoi(aoi);
            eventDTO.setEventDescription(eventDescription);

            // now look for the event in the database
            User user = em.find(User.class, logUserName);
            EventOrder eventOrder = em.find(EventOrder.class, eventId);
            if(eventOrder == null) {
                // no created yet so create it now
                eventOrder = new EventOrder();
                eventOrder.setId(eventId);
                eventOrder.setCreationTime(new Date());
                eventOrder.setLastUpdate(new Date());
                em.persist(eventOrder);
                // TODO - we could add some tracking for a user
            }

            // if the event order has product orders, add them to the event DTO
            List<ProductOrder> productsOrdered = eventOrder.getProductsOrdered();
            if(productsOrdered != null && productsOrdered.size() > 0) {
                // call the publisher to get the information on products
                // sort product orders by creation date first
                Collections.sort(eventOrder.getProductsOrdered(), (o1, o2) -> o1.getCreationTime() == null ? -1 :
                        o2.getCreationTime() == null ? 1 :
                                o2.getCreationTime().compareTo(o1.getCreationTime()));
                eventDTO.setProductsOrdered(ListUtil.mutate(eventOrder.getProductsOrdered(), new ListUtil.Mutate<ProductOrder, ProductOrderDTO>() {
                    @Override
                    public ProductOrderDTO mutate(ProductOrder productOrder) {
                        ProductOrderDTO productOrderDTO = convertProductOrderDTO(productOrder);
                        // check for additional data needed
                        if (productOrder.getStatus() == PRODUCTORDER_STATUS.Documentation && productOrder.getProductRequest() != null) {
                            productOrderDTO.setPolicyId(productOrder.getPolicyId());
                            // find product fetch task to get the ei order id
                            TypedQuery<ProductFetchTask> query = em.createQuery("select p from ProductFetchTask p where p.productOrder = :productOrder", ProductFetchTask.class);
                            query.setParameter("productOrder", productOrder);
                            List<ProductFetchTask> productFetchTasks = query.getResultList();
                            if (productFetchTasks.size() == 1) {
                                productOrderDTO.setEIOrderId(productFetchTasks.get(0).getEIOrderId());
                            }
                        }
                        // add converted offered price
                        if (productOrderDTO.getOfferedPrice() != null) {
                            try {
                                productOrderDTO.setConvertedOfferedPrice(ServerUtil.getCurrencyRateTable().getConvertedPrice(productOrderDTO.getOfferedPrice(), user.getCredit().getCurrency()));
                            } catch (EIException e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                        return productOrderDTO;
                    }
                }));
                // TODO - check which ones have been purchased by the user

            }
            // now add the existing imagery which could be requested
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setAoiWKT(AOIUtils.toWKT(aoi));
            searchRequest.setStart(eventDTO.getEventDescription().getStartDate());
            Date endDate = eventDTO.getEventDescription().getEndDate();
            // no end date yet the event is still ongoing
            if(endDate == null) {
                // add 3 days to see what acquisitions are coming next
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 3);
                endDate = calendar.getTime();
            }
            searchRequest.setStop(endDate);
            // TODO - select sensors based on size of AoI and event
            SensorFilters sensorFilters = new SensorFilters();
            sensorFilters.setType(SENSOR_TYPE.Optical);
            sensorFilters.setMaxResolution(10.0);
            SearchResponse searchResponse = EIAPIUtil.queryProducts(searchRequest);
            // add potential products to the event DTO
            eventDTO.setProductCandidates(searchResponse.getProducts());
            return eventDTO;
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private AOI convertGeoJSONToAOI(Geometry geometry) {
        switch (geometry.getType()) {
            case "Point":
                // create a circle
                AOICircle aoiCircle = new AOICircle();
                aoiCircle.setCenter(convertCoordinates(((Point) geometry).getCoordinates()));
                // TODO - we need to find a way to add radius
                aoiCircle.setRadius(1000);
                return aoiCircle;
            case "Polygon":
                AOIPolygon aoiPolygon = new AOIPolygon();
                aoiPolygon.setPoints(convertCoordinates(((Polygon) geometry).getCoordinates().get(0)));
                break;
            case "MultiPolygon":
            case "LineString":
            default:
        }
        return null;
    }

    private List<EOLatLng> convertCoordinates(List<double[]> coordinates) {
        List<EOLatLng> convertedCoordinates = new ArrayList<EOLatLng>();
        for(double[] coordinate : coordinates) {
            convertedCoordinates.add(convertCoordinates(coordinate));
        }
        return convertedCoordinates;
    }

    private EOLatLng convertCoordinates(double[] coordinates) {
        return new EOLatLng(coordinates[1], coordinates[0]);
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

    // stop following the event but keep the subscribed products
    @Override
    public void archiveEvent(String eventId) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());
        if(eventId == null) {
            throw new EIException("Order id missing");
        }
        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);
            EventOrder eventOrder = em.find(EventOrder.class, eventId);
            if(eventOrder == null) {
                throw new EIException("Could not find event with id " + eventId);
            }
            // TODO - handle the unsubscribe request from user
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

            EventOrder eventOrder = productOrder.getEventOrder();
            eventOrder.getProductsOrdered().remove(productOrder);
            em.remove(productOrder);
            OrderHelper.updateOrderStatus(eventOrder);
            em.getTransaction().commit();


            //Trying to delete the product from the disk
            if(productOrder.getFileLocation() != null) {
/*
                File productDirectory = new File(productOrder.getFileLocation()).getParentFile();
                FileUtils.deleteDirectory(productDirectory);
*/
                new File(productOrder.getFileLocation()).delete();
            }

            NotificationSocket.notifyOrderChanged(user, eventOrder);

        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<EventSummaryDTO> loadEvents(String name, Date start, Date stop, String status) throws EIException {
        String logUserName = ServerUtil.validateUser(getThreadLocalRequest());

        if(start.getTime() > stop.getTime() || start.getTime() > new Date().getTime()) {
            throw new EIException("Start should be before stop date");
        }

        EntityManager em = EMF.get().createEntityManager();
        try {
            User user = em.find(User.class, logUserName);

            List<com.geocento.webapps.earthimages.emis.application.server.eventsapi.EventDTO> events = EventsAPIUtil.loadEvents();
            return ListUtil.mutate(events, (ListUtil.Mutate<com.geocento.webapps.earthimages.emis.application.server.eventsapi.EventDTO, EventSummaryDTO>) eventDTO -> {
                EventSummaryDTO eventSummaryDTO = new EventSummaryDTO();
                eventSummaryDTO.setId(eventDTO.getProperties().getEventUID());
                eventSummaryDTO.setName(eventDTO.getProperties().getTitle());
                eventSummaryDTO.setDescription(eventDTO.getProperties().getDescription());
                try {
                    eventSummaryDTO.setCreationDate(ServerUtil.parseDate(eventDTO.getProperties().getStartDate()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                AOI aoi = convertGeoJSONToAOI(eventDTO.getGeometry());
                eventSummaryDTO.setAoi(aoi);
                // TODO - add subscribers and number of products available
                return eventSummaryDTO;
            });
        } catch(Exception e) {
            throw handleException(em, e);
        } finally {
            em.close();
        }
    }

    private List<AOI> getAoisFromProductOrders(List<ProductOrder> productOrders) {
        List<AOI> aois = new ArrayList<AOI>();

        for (ProductOrder productOrder : productOrders) {
            aois.add(productOrder.getAoi());
        }

        return aois;
    }

    private EOBounds getBoundFromProductOrders(List<ProductOrder> productOrders) {
        double minx = 180;
        double maxx = -180;
        double miny = 90;
        double maxy= -90;

        for (ProductOrder productOrder : productOrders) {
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

    private void checkWorkspaceUser(Workspace workspace, User dbUser) throws EIException {
        if(workspace == null) {
            throw new EIException("Unknown workspace");
        }
        if(dbUser.getUserRole() != USER_ROLE.ADMINISTRATOR && !workspace.getOwner().getUsername().contentEquals(dbUser.getUsername())) {
            throw new EIException("Access not allowed");
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
            for(ProductOrder productOrder : order.getProductsOrdered()) {
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

            // TODO - check user has purchased this product
/*
            if(!ServerUtil.isUserAdministrator(getThreadLocalRequest()) && !productOrder.getEventOrder().getOwner().getUsername().contentEquals(logUserName)) {
                throw new EIException("Not allowed");
            }
*/

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