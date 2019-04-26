package com.geocento.webapps.earthimages.emis.common.server.publishapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductFetchTask;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductOrder;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductPublishRequest;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductRequest;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.STATUS;
import com.geocento.webapps.earthimages.emis.application.server.imageapi.EIAPIUtil;
import com.geocento.webapps.earthimages.emis.application.share.ProductMetadataDTO;
import com.geocento.webapps.earthimages.productdownloader.DOWNLOAD_TYPES;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.DownloadProductRequest;
import com.geocento.webapps.earthimages.productdownloader.api.dtos.methods.*;
import com.geocento.webapps.earthimages.productpublisher.api.dtos.ProductMetadata;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gwt.http.client.RequestException;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 28/04/2017.
 */
public class PublishAPIUtils {

    static Logger logger = Logger.getLogger(PublishAPIUtils.class);
    static {
        logger.info("Starting customer service");
   };

    public static DownloadProductRequest getDownloadProductRequest(ProductOrder productOrder) throws RequestException {
        ProductRequest product = productOrder.getProductRequest();
        try {
            DownloadProductRequest downloadProductRequest = new DownloadProductRequest();
            MethodValues.DownloadMethod method = getDownloadMethodValue(product.getSatelliteName());
            downloadProductRequest.setSupplierId(product.getProviderName() == null ? "UNKNOWN" : product.getProviderName());
            // no need for reference id in this case
            downloadProductRequest.setReferenceId(null);
            DOWNLOAD_TYPES downloadType = DOWNLOAD_TYPES.valueOf(method.downloadMethod);
            downloadProductRequest.setDownloadType(downloadType);
            switch (downloadType) {
                case SEDASAPI: {
                    SEDASAPIProperties sedasapiProperties = new SEDASAPIProperties();
                    sedasapiProperties.setSedasProductId(product.getProviderId());
                    downloadProductRequest.setSedasapiProperties(sedasapiProperties);
                } break;
                case SCIHUB_API: {
                    SciHubAPIProperties sciHubAPIProperties = new SciHubAPIProperties();
                    sciHubAPIProperties.setEsaProductId(product.getProviderId());
                    downloadProductRequest.setSciHubAPIProperties(sciHubAPIProperties);
                } break;
                case USGS_API: {
                    USGSAPIProperties usgsapiProperties = new USGSAPIProperties();
                    switch (productOrder.getProductRequest().getSatelliteName().toUpperCase()) {
                        case "LANDSAT-7":
                            usgsapiProperties.setPlatformName("LANDSAT7");
                            break;
                        case "LANDSAT-8":
                            usgsapiProperties.setPlatformName("LANDSAT8");
                            break;
                    }
                    // we need to add some 00 string for it to work...
                    usgsapiProperties.setUsgsProductId(product.getProviderId());
                    downloadProductRequest.setUsgsapiProperties(usgsapiProperties);
                }
                break;
                case LANDSAT8_AWS_URL: {
                    // the aws service uses the product id and not the scene id
                    // need to fetch the product...
                    String productId = product.getGeocentoid();
                    List<Product> eiProduct = EIAPIUtil.queryProductsById(productId);
                    if(eiProduct.size() == 0) {
                        throw new EIException("Could not find product with id " + productId);
                    }
                    JsonElement vendorAttributes = new JsonParser().parse(eiProduct.get(0).getVendorAttributes());
                    try {
                        productId = vendorAttributes.getAsJsonObject().get("Landsat Product ID").getAsString();
                    } catch (Exception e) {
                        throw new EIException("Could not get Landsat ID");
                    }
                    LSATAWSAPIProperties lsatawsapiProperties = new LSATAWSAPIProperties();
                    lsatawsapiProperties.setUsgsProductId(productId);
                    // we only need bands for true colour
                    int[] bands = new int[] {1, 2, 3, 4, 5, 6, 7, 8};
                    lsatawsapiProperties.setBands(bands);
                    downloadProductRequest.setLsatawsapiProperties(lsatawsapiProperties);
                }
                break;
                case PRODUCT_DOWNLOADER: {
/*
                    ProductDownloaderProperties productDownloaderProperties = new ProductDownloaderProperties();
                    productDownloaderProperties.setUrl();
                    productDownloaderProperties.setStatsId(pro);
                    downloadProductRequest.setProductDownloaderProperties(productDownloaderProperties);
*/
                } break;
                case LOCAL_FTP: {
                    FTPPushProperties ftpPushProperties = new FTPPushProperties();
                    // products are expected to be pushed into the
                    ftpPushProperties.setDirectory("");
                } break;
                default:
                    throw new RequestException("Unsupported download type " + downloadType);
            }
            return downloadProductRequest;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RequestException(e.getMessage());
        }
    }

    public static MethodValues.DownloadMethod getDownloadMethodValue(String satelliteName) throws Exception {
        MethodValues methodValues = getMethodValues();
        final String platformName = getPlatformName(satelliteName);
        return ListUtil.findValue(methodValues.downloadMethods, value -> {
            for (String platform : value.platforms) {
                if (platformName.matches(platform)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static MethodValues.PublishMethod getPublishMethodValue(String satelliteName) throws Exception {
        MethodValues methodValues = getMethodValues();
        final String platformName = getPlatformName(satelliteName);
        return ListUtil.findValue(methodValues.publishMethods, value -> {
            for (String platform : value.platforms) {
                if (platformName.matches(platform)) {
                    return true;
                }
            }
            return false;
        });
    }

    private static MethodValues getMethodValues() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String downloadMethodsJSON = FileUtils.readFileToString(new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.downloadMethodsPath))); //Utils.getSettings().getDownloadMethods();
        MethodValues methodValues = objectMapper.readValue(downloadMethodsJSON, MethodValues.class);
        return methodValues;
    }

    public static ProductFetchTask createProductFetchTask(ProductOrder productOrder) {
        // save a task for fetching and storing image
        ProductFetchTask productFetchTask = new ProductFetchTask();
        // make sure some key values are initialised adequately
        productFetchTask.setProductOrder(productOrder);
        productFetchTask.setCreated(new Date());
        productFetchTask.setCompleted(null);
        productFetchTask.setStatus(STATUS.created);
        productFetchTask.setStatusMessage(null);
        productFetchTask.setCreated(new Date());
        productFetchTask.setFetchDate(new Date());
        return productFetchTask;
    }

    public static String getPlatformName(String satelliteName) throws Exception {
        // return the same for now
        // we might need to do some conversion at some point
        MethodValues methodValues = getMethodValues();
        MethodValues.Platform platform = ListUtil.findValue(methodValues.platforms, new ListUtil.CheckValue<MethodValues.Platform>() {
            @Override
            public boolean isValue(MethodValues.Platform value) {
                return StringUtils.join(value.values, "<>").contains(satelliteName.toUpperCase());
            }
        });
        // if it couldn't be found, defaults to the satellite name in upper case
        if(platform == null) {
            //throw new Exception("Could not find platform for satellite " + satelliteName);
            return satelliteName.toUpperCase();
        }
        return platform.name;
    }

    public static boolean isLocallyFetched(String satelliteName) throws Exception {
        MethodValues methodValues = getMethodValues();
        String platformName = getPlatformName(satelliteName);
        return methodValues.freePlatforms.contains(platformName);
    }

    public static boolean isPlanetOrdered(String satelliteName) {
        return satelliteName.startsWith("PLANET");
    }

    static public ProductMetadataDTO convertProductMetadata(ProductMetadata productMetadata, ProductPublishRequest publishRequest) {
        ProductMetadataDTO productMetadataDTO = new ProductMetadataDTO();
        productMetadataDTO.setPublishRequestId(publishRequest.getId());
        productMetadataDTO.setName(productMetadata.getName());
        productMetadataDTO.setDescription(productMetadata.getDescription());
        productMetadataDTO.setPublishUri(productMetadata.getPublishUri());
        productMetadataDTO.setProductType(productMetadata.getProductType());
        productMetadataDTO.setCoordinatesWKT(productMetadata.getCoordinatesWKT());
        return productMetadataDTO;
    }

/*
    private File createThumbnail(ProductOrder product) {
        try {
            // create file for thumbnail
            File file = new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.baseThumbnailsDirectory) + "/" +
                    product.getOwner() + "/" +
                    product.getProductId() + ".jpg");
            Extent extent = product.getExtent();
            // we want a square image
            double lngExtent = extent.getEast() - extent.getWest();
            double latExtent = extent.getNorth() - extent.getSouth();
            double ratio = lngExtent / latExtent;
            // if larger than high we need to extend the height
            if (ratio > 1) {
                double margin = latExtent * (ratio - 1) / 2;
                extent.setSouth(extent.getSouth() - margin);
                extent.setNorth(extent.getNorth() + margin);
            } else {
                double margin = lngExtent * (ratio - 1) / 2;
                extent.setWest(extent.getWest() - margin);
                extent.setEast(extent.getEast() + margin);
            }
            // define new bbox
            String bbox = extent.getWest() + "%2C" +
                    extent.getSouth() + "%2C" +
                    extent.getEast() + "%2C" +
                    extent.getNorth();
            FileUtils.copyURLToFile(new URL(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.geoserverUri) + "?" +
                            "SERVICE=WMS&" +
                            "VERSION=1.1.1&" +
                            "REQUEST=GetMap&" +
                            "FORMAT=image%2Fjpeg&" +
                            "LAYERS=" + product.getLayerName() +
                            "&SRS=EPSG%3A4326" +
                            "&WIDTH=300" +
                            "&HEIGHT=300&" +
                            "BBOX=" + bbox),
                    file);
            return file;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
*/

}
