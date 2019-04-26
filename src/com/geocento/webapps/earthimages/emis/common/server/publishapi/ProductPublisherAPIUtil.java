package com.geocento.webapps.earthimages.emis.common.server.publishapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductLayer;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.*;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by thomas on 28/07/2017.
 */
public class ProductPublisherAPIUtil {

    static Logger logger = Logger.getLogger(ProductPublisherAPIUtil.class);

    static private String baseUrl = Configuration.getProperty(Configuration.APPLICATION_SETTINGS.productPublisherUrl);

    static public PublishProductRequestResponse createTask(PublishProductRequest publishProductRequest) {
        Client client = ClientBuilder.newClient();

        PublishProductRequestResponse result = client
                .target(baseUrl + "/publish-product")
                .request()
                .post(Entity.entity(publishProductRequest, "application/json"), PublishProductRequestResponse.class);

        return result;
    }

    static public PublishProductRequestResponse createTask(File file, PublishProductRequest publishProductRequest) throws JsonProcessingException {

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        final Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build()
                .property(ClientProperties.CHUNKED_ENCODING_SIZE, 1024)
                .property(ClientProperties.REQUEST_ENTITY_PROCESSING, "CHUNKED");

        WebTarget t = client.target(baseUrl + "/publish-product/file"); //.path("multipart").path("upload2");

        FileDataBodyPart filePart = new FileDataBodyPart("file", file);

        String publishProductRequestString = new ObjectMapper().writeValueAsString(publishProductRequest);

        MultiPart multipartEntity = new FormDataMultiPart()
                .field("request", publishProductRequestString, MediaType.APPLICATION_JSON_TYPE)
                .bodyPart(filePart);

        PublishProductRequestResponse result = t.request()
                .post(Entity.entity(multipartEntity, multipartEntity.getMediaType()), PublishProductRequestResponse.class);

        return result;
    }

    public static List<PublishProductProcessDTO> getProcesses(int start, int limit, String name, String platformName, String productTypeIds, String orderBy) throws ExecutionException, InterruptedException {
        Client client = ClientBuilder.newClient();

        List<PublishProductProcessDTO> result = client
                .target(baseUrl + "/processes")
                .queryParam("start", start)
                .queryParam("limit", limit)
                .queryParam("name", name)
                .queryParam("platform", platformName)
                .queryParam("productTypeIds", productTypeIds)
                .queryParam("tags", "production")
                .queryParam("orderBy", orderBy)
                .request()
                .buildGet()
                .submit(new GenericType<List<PublishProductProcessDTO>>() {
                }).get();

        return result;
    }

    public static PublishProcessProducts getProductsPublished(Long publishTaskId) throws Exception {
        Client client = ClientBuilder.newClient();

        String url = baseUrl + "/publish-product/" + publishTaskId + "/stats";
        Response response = client
                .target(url)
                .request()
                .get();

        if(response.getStatus() >= 300) {
            throw new Exception("Error calling publish API with URL " + url + ", CODE " + response.getStatus() + " and message is " + response.readEntity(String.class));
        }
        return response.readEntity(PublishProcessProducts.class);
    }

    public static String getProductsPublishedLogs(Long publishTaskId) throws Exception {
        Client client = ClientBuilder.newClient();

        String url = baseUrl + "/publish-product/" + publishTaskId + "/stats/logs";
        Response response = client
                .target(url)
                .request()
                .get();

        if(response.getStatus() >= 300) {
            throw new Exception("Error calling publish API with URL " + url + ", CODE " + response.getStatus() + " and message is " + response.readEntity(String.class));
        }
        return response.readEntity(String.class);
    }

    public static void deleteProduct(Long publishTaskId) {
        Client client = ClientBuilder.newClient();

        client.target(baseUrl + "/publish-product/" + publishTaskId)
                .request()
                .delete();

    }

    public static ProductPublisherTaskDTO getProductPublisherTask(Long publishTaskId) {
        Client client = ClientBuilder.newClient();

        ProductPublisherTaskDTO result = client
                .target(baseUrl + "/publish-product/" + publishTaskId)
                .request()
                .get(ProductPublisherTaskDTO.class);

        return result;
    }

    public static boolean isPublisherLocal() {
        return Configuration.getBooleanProperty(Configuration.APPLICATION_SETTINGS.localPublisher);
    }

    public static String createGroupLayers(String workspace, String description, List<ProductLayer> layers) throws ExecutionException, InterruptedException, EIException {
        PublishGroupLayerRequest publishGroupLayerRequest = createGroupLayerRequest(workspace, description, layers);

        Client client = ClientBuilder.newClient();

        Response response = client
                .target(baseUrl + "/publish-product/groupLayer")
                .request()
                .buildPost(Entity.json(publishGroupLayerRequest))
                .submit()
                .get();

        if(response.getStatus() >= 300) {
            throw new EIException(response.readEntity(String.class));
        }
        return response.readEntity(PublishGroupLayerResponse.class).getLayerId();
    }

    public static String updateGroupLayers(String workspace, String layerId, String description, List<ProductLayer> layers) throws ExecutionException, InterruptedException, EIException {
        PublishGroupLayerRequest publishGroupLayerRequest = createGroupLayerRequest(workspace, description, layers);

        Client client = ClientBuilder.newClient();

        Response response = client
                .target(baseUrl + "/publish-product/groupLayer/" + layerId)
                .request()
                .buildPut(Entity.json(publishGroupLayerRequest))
                .submit()
                .get();

        if(response.getStatus() >= 300) {
            throw new EIException(response.readEntity(String.class));
        }
        return response.readEntity(PublishGroupLayerResponse.class).getLayerId();
    }

    public static void removeGroupLayers(String publishedWorkspace, String publishedId) throws Exception {
        Client client = ClientBuilder.newClient();

        Response response = client
                .target(baseUrl + "/publish-product/groupLayer/" + publishedWorkspace + "/" + publishedId)
                .request()
                .buildDelete()
                .submit()
                .get();

        if(response.getStatus() >= 300) {
            throw new EIException(response.readEntity(String.class));
        }
    }

    private static PublishGroupLayerRequest createGroupLayerRequest(String workspace, String description, List<ProductLayer> layers) {
        PublishGroupLayerRequest publishGroupLayerRequest = new PublishGroupLayerRequest();
        publishGroupLayerRequest.setTargetWorkspace(workspace);
        publishGroupLayerRequest.setLayerDescription(description);
        publishGroupLayerRequest.setSourceLayers(ListUtil.mutate(layers, new ListUtil.Mutate<ProductLayer, LayerValue>() {
            @Override
            public LayerValue mutate(ProductLayer productLayer) {
                LayerValue layerValue = new LayerValue();
                layerValue.setName(workspace + ":" + productLayer.getPublishUri());
                layerValue.setSldName(productLayer.getSldName());
                return layerValue;
            }
        }));
        return publishGroupLayerRequest;
    }

    public static void getProductThumbnail(Long publishTaskId, int width, int height, File thumbnailFile) throws Exception {
        FileUtils.copyURLToFile(new URL(baseUrl + "/publish-product/download/products/thumbnail/" + publishTaskId + "?width=" + width + "&height=" + height), thumbnailFile);
    }

    public static String getLayerThumbnail(String workspace, String layerId, int width, int height) {
        String imagePath = workspace + "_" + layerId + "_thumb.jpeg";
        File thumbnailFile = new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.baseThumbnailsDirectory) + imagePath);
        try {
            FileUtils.copyURLToFile(new URL(baseUrl + "/publish-product/groupLayer/" + workspace + "/" + layerId + "/thumbnail?width=" + width + "&height=" + height), thumbnailFile);
            return "./thumbnails/" + imagePath;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
