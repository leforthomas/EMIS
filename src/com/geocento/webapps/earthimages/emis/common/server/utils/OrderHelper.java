package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.geocento.webapps.earthimages.emis.admin.server.publishapis.ProductDownloaderAPIUtil;
import com.geocento.webapps.earthimages.emis.admin.server.publishapis.ProductPublisherAPIUtil;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.geocento.webapps.earthimages.emis.application.server.utils.ProductRequestUtil;
import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.domain.*;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.map.utils.GeometryUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by thomas on 28/09/2017.
 */
public class OrderHelper {

    static private SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MMM-dd 'at' HH:mm:ss 'UTC'");
    static {
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private static DecimalFormat format = new DecimalFormat("#.##");

    static private KeyGenerator keyGenerator = new KeyGenerator(16);

    static public void updateOrderStatus(EventOrder eventOrder) {
        int completed = 0;
        int requested = 0;
        int inProduction = 0;
        int failed = 0;
        for(ProductOrder productOrder : eventOrder.getProductsOrdered()) {
            switch (productOrder.getStatus()) {
                case Completed:
                    completed++;
                    break;
                case Failed:
                    failed++;
                    completed++;
                    break;
                default:
                    inProduction++;
                    break;
            }
        }
    }

    public static ProductRequest createProductRequest(String searchId, AOI aoi, Product product) {
        // create the product request
        ProductRequest productRequest = new ProductRequest();
        productRequest.setCreationTime(new Date());
        productRequest.setSearchId(searchId);
        productRequest.setGeocentoid(product.getProductId());
        String providerId = product.getProviderId();
        productRequest.setProviderId(providerId);
        productRequest.setProviderName(product.getProviderName());
        productRequest.setPolicyId(product.getPolicyId());
        productRequest.setSatelliteName(product.getSatelliteName());
        productRequest.setSensorName(product.getInstrumentName());
        productRequest.setStart(product.getStart());
        productRequest.setCoordinatesWKT(product.getCoordinatesWKT());
        productRequest.setAoi(aoi);
        return productRequest;
    }

    public static TaskingRequest createTaskingRequest(String searchId, AOI aoi, Product product) {
        TaskingRequest taskingRequest = new TaskingRequest();
        taskingRequest.setProductId(product.getProductId());
        taskingRequest.setAoi(aoi);
        taskingRequest.setInstrumentId(product.getInstrumentId());
        taskingRequest.setProviderName(product.getProviderName());
        taskingRequest.setProductEntity(ProductRequestUtil.convertToProductEntity(product));
        taskingRequest.setCreationTime(new Date());
        taskingRequest.setSearchId(searchId);
        taskingRequest.setSearchId(searchId);
        taskingRequest.setPolicyId(product.getPolicyId());
        return taskingRequest;
    }

    public static ProductOrder createProductOrder(ProductRequest productRequest) {
        ProductOrder productOrder = new ProductOrder();
        productOrder.setId(keyGenerator.CreateKey());
        productOrder.setCreationTime(new Date());
        productOrder.setAoi(productRequest.getAoi());
        String aoiWKT = AOIUtils.toWKT(productRequest.getAoi());
        List<EOLatLng> selectionGeometry = productRequest.getSelectionGeometry();
        String selectionGeometryWKT = null;
        if(selectionGeometry != null) {
            selectionGeometryWKT = "POLYGON((" + EOLatLng.toWKT(com.metaaps.webapps.libraries.client.map.utils.GeometryUtils.closePath(selectionGeometry.toArray(new EOLatLng[selectionGeometry.size()]))) + "))";
        } else {
            selectionGeometryWKT = aoiWKT;
        }
        productOrder.setSelectionGeometry(selectionGeometryWKT);
        productOrder.setProductRequest(productRequest);
        productOrder.setTitle("Selection from " + productRequest.getSatelliteName() + " - " + productRequest.getSensorName() + " product");
        productOrder.setDescription("The original product was acquired on " + fmt.format(productRequest.getStart()) +
                " by the " + productRequest.getSensorName() + " sensor on the " + productRequest.getSatelliteName() + " satellite.");
        productOrder.setStatus(PRODUCTORDER_STATUS.Created);
        return productOrder;
    }

    public static ProductOrder createProductOrder(TaskingRequest taskingRequest) {
        ProductOrder productOrder = new ProductOrder();
        productOrder.setId(keyGenerator.CreateKey());
        productOrder.setCreationTime(new Date());
        productOrder.setAoi(taskingRequest.getAoi());
        String aoiWKT = AOIUtils.toWKT(taskingRequest.getAoi());
        List<EOLatLng> selectionGeometry = taskingRequest.getSelectionGeometry();
        String selectionGeometryWKT = null;
        if(selectionGeometry != null) {
            selectionGeometryWKT = "POLYGON((" + EOLatLng.toWKT(GeometryUtils.closePath(selectionGeometry.toArray(new EOLatLng[selectionGeometry.size()]))) + "))";
        } else {
            selectionGeometryWKT = aoiWKT;
        }
        productOrder.setSelectionGeometry(selectionGeometryWKT);
        productOrder.setTaskingRequest(taskingRequest);
        ProductEntity productEntity = taskingRequest.getProductEntity();
        productOrder.setTitle("Tasking request for " + productEntity.getSatelliteName() + " - " + productEntity.getInstrumentName() + " product");
        productOrder.setDescription("This product can be acquired on " + fmt.format(productEntity.getStart()) +
                " by the " + productEntity.getInstrumentName() + " sensor on the " + productEntity.getSatelliteName() + " satellite.");
        productOrder.setStatus(PRODUCTORDER_STATUS.Created);
        return productOrder;
    }

    private static String formatString(String value) {
        return value == null ?  "NA" : value;
    }

    private static String formatNumber(Integer value, String unitValue) {
        return value == null || value == -1 ?  "NA" : (value.toString() + " " +
                (unitValue == null ? "" : unitValue));
    }

    private static String formatNumber(Double value, String unitValue) {
        return value == null || value == -1 ?  "NA" : (format.format(value) + " " +
                (unitValue == null ? "" : unitValue));
    }

    public static File getProductDirectory(ProductOrder productOrder, boolean create) throws Exception {
        File productDirectory = new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.baseOrdersFileDirectory) +
                "/" + productOrder.getEventOrder().getId() + "/orders/");

        if (create && !productDirectory.exists() && !productDirectory.mkdirs()) {
            throw new Exception("Could not create file directory " + productDirectory.getAbsolutePath());
        }
        return productDirectory;
    }

    public static ProductSample createProductSample(ProductOrder productOrder) throws Exception {
        ProductSample productSample = new ProductSample();
        productSample.setId(keyGenerator.CreateKey());
        String geocentoId = productOrder.getProductRequest().getGeocentoid();
        List<Product> products = EIAPIUtil.queryProductsById(geocentoId);
        if(products.size() == 0) {
            throw new EIException("No products found for the product id " + geocentoId);
        }
        Product product = products.get(0);
        productSample.setInstrumentId(product.getInstrumentId());
        productSample.setProductOrder(productOrder);
        return productSample;
    }

    public static void removeProductOrder(EntityManager em, ProductOrder productOrder) throws IOException {
        TypedQuery<ProductFetchTask> query = em.createQuery("select p from ProductFetchTask p where p.productOrder.id = :productId", ProductFetchTask.class);
        query.setParameter("productId", productOrder.getId());
        List<ProductFetchTask> productFetchTasks = query.getResultList();

        for (ProductFetchTask productFetchTask : productFetchTasks) {
            Long downloadTaskId = productFetchTask.getDownloadTaskId();
            Long publishTaskId = productFetchTask.getPublishTaskId();
            em.remove(productFetchTask);
            if(downloadTaskId != null) {
                ProductDownloaderAPIUtil.deleteProduct(downloadTaskId);
            }
            if(publishTaskId != null) {
                ProductPublisherAPIUtil.deleteProduct(publishTaskId);
            }
        }

        EventOrder eventOrder = productOrder.getEventOrder();
        eventOrder.getProductsOrdered().remove(productOrder);
        em.remove(productOrder);
        OrderHelper.updateOrderStatus(eventOrder);

        //Trying to delete the product from the disk
        new File(productOrder.getFileLocation()).delete();
/*
        File productDirectory = new File(productOrder.getFileLocation()).getParentFile();
        FileUtils.deleteDirectory(productDirectory);
*/

    }


    public static List<ProductOrder> getOrderedProducts(EntityManager em, String userName, List<String> productIds) {
        User user = em.find(User.class, userName);
        TypedQuery<ProductOrder> query = em.createQuery("select p from ProductOrder p where p.order.owner = :owner and p.productRequest.geocentoid IN :productIds", ProductOrder.class);
        query.setParameter("owner", user);
        query.setParameter("productIds", productIds);
        return query.getResultList();
    }

    public static List<ProductOrder> getOrderedProducts(String logUserName, List<String> productIds) {
        EntityManager em = EMF.get().createEntityManager();
        try {
            return getOrderedProducts(em, logUserName, productIds);
        } finally {
            em.close();
        }
    }
}
