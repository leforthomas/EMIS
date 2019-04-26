package com.geocento.webapps.earthimages.emis.application.server.imageapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.earthimages.emis.common.server.utils.UrlUtils;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.google.gson.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.orders.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.GetPoliciesResponse;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.LicensingPolicy;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.ProductPolicy;
import com.metaaps.webapps.earthimages.extapi.server.domain.policies.SignedLicenseDTO;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.glassfish.jersey.uri.UriComponent;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by thomas on 04/03/2016.
 */
public class EIAPIUtil {

    static private Logger logger = Logger.getLogger(EIAPIUtil.class);

    static GsonBuilder builder = new GsonBuilder();
    static {
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });
    }

    public static SearchRequest loadSearchRequest(String searchId) throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "search/" + searchId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String message = getErrorMessage(response);
            logger.error("Error calling EI API, CODE " + response.getStatus() + " and message is " + message);
            throw new EIException("Error retrieving search, message is " + message);
        }
        return response.readEntity(SearchRequest.class);
    }

    static public SearchResponse queryProducts(SearchRequest searchRequest) throws Exception {
        Client client = ignoreSSLClient();

/*
        ProductFilters filter = searchRequest.getFilters();
        if(filter != null && filter.getCloud() == 0) {
            filter.setCloud(null);
            filter.setCloudMask(true);
        }
*/

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "search")
                .queryParam("withPrice", true)
                .queryParam("withCoverage", true)
                .queryParam("save", true)
                .queryParam("onlyOrderable", true)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildPost(Entity.json(searchRequest));
        Response response = invocation.submit().get();
        return processResponse(response);
    }

    // do some extra processing on products
    private static SearchResponse processResponse(Response response) throws EIException {
        if(response.getStatus() >= 300) {
            String message = getErrorMessage(response);
            logger.error("Error calling EI search API, CODE " + response.getStatus() + " and message is " + message);
            throw new EIException("Error calling search API, message is " + message);
        }
        SearchResponse searchResponse = response.readEntity(SearchResponse.class);
        MessageDigest messageDigest = null;
        try {
            // MD5 should be good enough for product ids
            messageDigest = MessageDigest.getInstance("MD5");
            for(Product product : searchResponse.getProducts()) {
                if(product.getType() == TYPE.TASKING) {
                    // TODO - round numbers so that the product id does not change with small changes
                    // add a product id based on a hash of the key parameters
                    String stringToEncrypt = StringUtils.join(new String[] {
                            product.getInstrumentId() + "", product.getModeName(),
                            product.getStart().getTime() + "", product.getStop().getTime() + "",
                            product.getCoordinatesWKT()
                            }, ",");
                    messageDigest.update(stringToEncrypt.getBytes());
                    String productId = Base64.encodeBase64String(messageDigest.digest());
                    productId = productId.substring(0, productId.lastIndexOf("=="));
                    product.setProductId(productId);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        return searchResponse;
    }

    static public SearchResponse queryProducts(String searchId) throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "search/" + searchId)
                .queryParam("withPrice", true)
                .queryParam("withCoverage", true)
                .queryParam("onlyOrderable", true)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildPost(null);
        Response response = invocation.submit().get();
        return processResponse(response);
    }

    public static List<Product> queryProductsById(String productIds, boolean geocentoIds) throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "search/products")
                .queryParam("ids", productIds)
                .queryParam("geocento", geocentoIds)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildPost(Entity.json(null));
        Response response = invocation.submit().get();
        return processResponse(response).getProducts();
    }

    public static List<Product> queryProductsById(String productIds) throws Exception {
        return queryProductsById(productIds, true);
    }

    public static List<Product> queryProductsBySupplierId(String productIds) throws Exception {
        return queryProductsById(productIds, false);
    }

    public static AoIsImport importAoI(String fileName, InputStream fileContentStream) throws Exception {

        final Client client = ignoreSSLClient();
        client.register(MultiPartFeature.class);

        final StreamDataBodyPart filePart = new StreamDataBodyPart("file", fileContentStream, fileName);
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.bodyPart(filePart);

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "geometry/")
                .request()
                .header("Authorization", getToken())
                .buildPost(Entity.entity(multipart, multipart.getMediaType()));
        return invocation.submit(AoIsImport.class).get();
    }

    public static List<Satellite> listSatellites() throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "resources/satellites")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(new GenericType<List<Satellite>>(){}).get();
    }

    public static List<ProductPolicy> getCataloguePoliciesInstrument(List<Long> instrumentIds) throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "policy/catalogue/instruments?" +
                        ListUtil.toString(instrumentIds, new ListUtil.GetLabel<Long>() {
                            @Override
                            public String getLabel(Long value) {
                                return "ids=" + value;
                            }
                        }, "&"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(GetPoliciesResponse.class).get().getPolicies();
    }

    public static List<ProductPolicy> getTaskingPoliciesInstrument(List<Long> instrumentIds) throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "policy/tasking/instruments?" +
                ListUtil.toString(instrumentIds, new ListUtil.GetLabel<Long>() {
                    @Override
                    public String getLabel(Long value) {
                        return "ids=" + value;
                    }
                }, "&"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(GetPoliciesResponse.class).get().getPolicies();
    }

    public static File fetchFile(String eiOrderId, Long eiProductOrderId, File productDirectory) throws Exception {
        return UrlUtils.downloadProductFromHTTP(Utils.getSettings().getEartimagesAPIURL() + "download/download/" + eiOrderId + "/" + eiProductOrderId,
                productDirectory.getAbsolutePath(), eiProductOrderId + "", getToken());
    }

    public static CreateOrderResponse createOrderRequest(CreateOrderRequest createOrderRequest) throws Exception {
        Client client = ignoreSSLClient();
        client.register(new LoggingFilter(java.util.logging.Logger.getAnonymousLogger(), true));

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "orders")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildPost(Entity.json(createOrderRequest));
        Response response = invocation.submit().get();
        if(response.getStatus() >= 300) {
            String message = getErrorMessage(response);
            logger.error("ERROR submitting order request to API, message is " + message);
            throw new EIException(message);
        }
        return response.readEntity(CreateOrderResponse.class);
    }

    private static String getErrorMessage(Response response) {
        try {
            return response.readEntity(StatusResponse.class).getErrorMessage();
        } catch (Exception e) {
            return "server error";
        }
    }

    public static com.metaaps.webapps.earthimages.extapi.server.domain.orders.Order getOrder(String eiOrderId) throws ExecutionException, InterruptedException {
        Client client = ignoreSSLClient();
        Invocation invocation = client.target(
                Utils.getSettings().getEartimagesAPIURL() + "orders/" + eiOrderId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(com.metaaps.webapps.earthimages.extapi.server.domain.orders.Order.class).get();
    }

    public static CatalogueProductOrder getCatalogueProductOrder(String eiOrderId, Long eiProductOrderId) throws ExecutionException, InterruptedException {
        Client client = ignoreSSLClient();
        Invocation invocation = client.target(
                Utils.getSettings().getEartimagesAPIURL() + "orders/" + eiOrderId + "/catalogue/" + eiProductOrderId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(CatalogueProductOrder.class).get();
    }

    public static TaskingProductOrder getTaskingProductOrder(String eiOrderId, Long eiProductOrderId) throws ExecutionException, InterruptedException {
        Client client = ignoreSSLClient();
        Invocation invocation = client.target(
                Utils.getSettings().getEartimagesAPIURL() + "orders/" + eiOrderId + "/tasking/" + eiProductOrderId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(TaskingProductOrder.class).get();
    }

    private static String getToken() {
        return "Token " + Utils.getSettings().getEarthimagesAPIToken();
    }

    private static String getRequestAsXMLString(Object request) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(request.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(request, stringWriter);
        return stringWriter.toString();
    }

    public static Client ignoreSSLClient() {

        try {
            SSLContext sslcontext = SSLContext.getInstance("TLS");

            sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new java.security.SecureRandom());

            return ClientBuilder.newBuilder()
                    .sslContext(sslcontext)
                    .hostnameVerifier((s1, s2) -> true)
                    .build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ClientBuilder.newClient();
        }
    }

    public static List<Satellite> listSatellites(SensorFilters sensorFilters) throws JsonProcessingException, ExecutionException, InterruptedException {
        Client client = ignoreSSLClient();

        ObjectMapper objectMapper = new ObjectMapper();
        String filter = objectMapper.writeValueAsString(sensorFilters);
        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "resources/satellites/filters/characteristics")
                .queryParam("filter", UriComponent.encode(filter, UriComponent.Type.QUERY_PARAM_SPACE_ENCODED))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(new GenericType<List<Satellite>>(){}).get();
    }

    public static ProductPolicy getPolicy(Long policyId) throws ExecutionException, InterruptedException {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "policy/" + policyId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(ProductPolicy.class).get();
    }

    public static SignedLicenseDTO signEULA(SignLicenseRequest signLicenseRequest) throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "policy/license/signlicense")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildPost(Entity.json(signLicenseRequest));
        return invocation.submit(SignedLicenseDTO.class).get();
    }

    public static LicensingPolicy getLicensingPolicy(Long policyId) throws Exception {
        Client client = ignoreSSLClient();

        Invocation invocation = client.target(Utils.getSettings().getEartimagesAPIURL() + "policy/license/" + policyId)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", getToken())
                .buildGet();
        return invocation.submit(LicensingPolicy.class).get();
    }

    public static URLConnection getEULADocumentUrl(Long eulaDocumentId) throws Exception {
        URLConnection connection = new URL(Utils.getSettings().getEartimagesAPIURL() + "policy/license/eula/" + eulaDocumentId).openConnection();
        connection.addRequestProperty("Authorization", "Token " + getToken());
        return connection;
    }

    public static void downloadFile(File eulaPDFFile, String signedLicenseUrlValue) throws Exception {
        UrlUtils.downloadFileFromHTTP(signedLicenseUrlValue, eulaPDFFile,getToken());
    }
}
