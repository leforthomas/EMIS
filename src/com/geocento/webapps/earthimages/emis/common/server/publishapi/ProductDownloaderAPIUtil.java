package com.geocento.webapps.earthimages.emis.common.server.publishapi;

import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.utils.UrlUtils;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.DownloadProductRequest;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.DownloadProductRequestResponse;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.ProductDownloadStatsDTO;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.ProductDownloaderTaskDTO;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Created by thomas on 28/07/2017.
 */
public class ProductDownloaderAPIUtil {

    static private Logger logger = Logger.getLogger(ProductDownloaderAPIUtil.class);

    static private String baseUrl = Configuration.getProperty(Configuration.APPLICATION_SETTINGS.productDownloaderUrl);

    static public DownloadProductRequestResponse createTask(DownloadProductRequest downloadProductRequest) {
        Client client = ClientBuilder.newClient();

        DownloadProductRequestResponse result = client
                .target(baseUrl + "/download-product")
                .request()
                .post(Entity.entity(downloadProductRequest, "application/json"), DownloadProductRequestResponse.class);

        return result;
    }

    static public ProductDownloadStatsDTO getDownloadStats(Long taskId) throws ExecutionException, InterruptedException {
        Client client = ClientBuilder.newClient();

        ProductDownloadStatsDTO result = client
                .target(baseUrl + "/download-product/stats/" + taskId)
                .request()
                .buildGet()
                .submit(ProductDownloadStatsDTO.class)
                .get();

        return result;
    }

    public static void deleteProduct(Long publishTaskId) {
        Client client = ClientBuilder.newClient();

        client.target(baseUrl + "/download-product/" + publishTaskId)
                .request()
                .delete();

    }

    public static File fetchFile(Long downloadTaskId, File productDirectory) throws Exception {

        ProductDownloadStatsDTO productDownloadStatsDTO = getDownloadStats(downloadTaskId);

        String url = productDownloadStatsDTO.getDownloadUrl();

        logger.info("Fetching from downloader with file id " + downloadTaskId + " at URL " + url);

        return UrlUtils.downloadProductFromHTTP(url, productDirectory.getAbsolutePath(), downloadTaskId + ".zip");
    }

    public static ProductDownloaderTaskDTO getTaskDTO(Long downloadTaskId) {
        Client client = ClientBuilder.newClient();

        ProductDownloaderTaskDTO result = null;
        try {
            result = client
                    .target(baseUrl + "/download-product/" + downloadTaskId)
                    .request()
                    .buildGet()
                    .submit(ProductDownloaderTaskDTO.class).get();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }
}
