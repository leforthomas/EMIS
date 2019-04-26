package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.utils.Utils;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.account.AccountResponse;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.account.AccountValues;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.model.ClipRequest;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.model.ClipResponse;
import com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.modelv2.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.Satellite;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

// TODO - move to a more generic set of classes
public class PlanetAPI {

    static Logger logger = Logger.getLogger(PlanetAPI.class);

    public enum PLATFORM {PSScene3Band, PSScene4Band, SkySatScene};
    public enum ASSET_TYPE {analytic, analytic_dm, visual};

    static String APIUrl = "https://api.planet.com/data/v1/item-types/$platform/items/$id";
    static String assetsAPIUrl = APIUrl + "/assets";
    static String clipAPIUrl = "https://api.planet.com/compute/ops/clips/v1";
    static String orderAPIv2 = "https://api.planet.com/compute/ops/orders/v2";

    static String accountAPIUrl = "https://api.planet.com/auth/v1/experimental/public/my/subscriptions";

    static public String requestClipUrl(PLATFORM platform, ASSET_TYPE assetType, String sceneId, String selectionGeometry, boolean demoAccount) throws EIException, ParseException {

        Client client = ClientBuilder.newClient();

        Coordinate[] coordinates = new WKTReader().read(selectionGeometry).getCoordinates();
        double[][] values = new double[coordinates.length][2];
        int index = 0;
        int numberDecimals = 4;
        for(Coordinate coordinate : coordinates) {
            values[index][0] = Utils.round(coordinate.x, numberDecimals);
            values[index][1] = Utils.round(coordinate.y, numberDecimals);
            index++;
        }
        ClipRequest clipRequest = new ClipRequest();
        ClipRequest.Aoi aoi = new ClipRequest.Aoi();
        aoi.type = "Polygon";
        aoi.coordinates = new double[][][] {values};
        clipRequest.aoi = aoi;
        ClipRequest.Targets targets = new ClipRequest.Targets();
        targets.asset_type = assetType.toString();
        targets.item_id = sceneId;
        targets.item_type = platform.toString();
        clipRequest.targets = ListUtil.toList(targets);
        Response result = null;
        try {
            result = client
                    .target(clipAPIUrl)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", getAPIToken(demoAccount))
                    .buildPost(Entity.json(clipRequest))
                    .submit()
                    .get();
            if(result.getStatus() >= 300) {
                logger.debug("Payload is " + new ObjectMapper().writeValueAsString(clipRequest));
                throw new Exception("method returned " + result.getStatus());
            }
            //logger.debug(result.readEntity(String.class));
            ClipResponse clipResponse = result.readEntity(ClipResponse.class);
            return clipResponse._links._self;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EIException("Error requesting planet product #" + sceneId + ", reason is " + e.getMessage());
        }
    }

    public static String getDownloadClipUrl(String clipUrl, boolean demoAccount) throws EIException {
        Client client = ClientBuilder.newClient();

        Response result = null;
        try {
            result = client
                    .target(clipUrl)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", getAPIToken(demoAccount))
                    .buildGet()
                    .submit()
                    .get();
            if (result.getStatus() >= 300) {
                throw new Exception("method returned " + result.getStatus());
            }
            ClipResponse clipResponse = result.readEntity(ClipResponse.class);
            if(clipResponse._links.results != null && clipResponse._links.results.length > 0) {
                return clipResponse._links.results[0];
            }
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EIException("Error fetching clip results with URL " + clipUrl + ", reason is " + e.getMessage());
        }
    }

    static public String requestOrderV2(PLATFORM platform, ASSET_TYPE assetType, String sceneId, String selectionGeometry, boolean demoAccount) throws EIException, ParseException {

        Client client = ClientBuilder.newClient();

        Coordinate[] coordinates = new WKTReader().read(selectionGeometry).getCoordinates();
        double[][] values = new double[coordinates.length][2];
        int index = 0;
        for(Coordinate coordinate : coordinates) {
            values[index][0] = coordinate.x;
            values[index][1] = coordinate.y;
            index++;
        }
        OrderRequest orderRequest = new OrderRequest();
        Aoi aoi = new Aoi();
        aoi.type = "Polygon";
        aoi.coordinates = new double[][][] {values};
        Clip clip = new Clip();
        clip.aoi = aoi;
        Tools tools = new Tools();
        tools.clip = clip;
        orderRequest.tools = ListUtil.toList(tools);

        Products products = new Products();
        products.itemIds = ListUtil.toList(sceneId);
        products.itemType = platform.toString();
        products.productBundle = assetType.toString();
        orderRequest.products = ListUtil.toList(products);

        Delivery delivery = new Delivery();
        delivery.archiveType = "zip";
        delivery.archiveFilename = "{{name}}_{{order_id}}.zip";
        orderRequest.delivery = delivery;

        orderRequest.name = "Request for scene id " + sceneId;

        //orderRequest.subscriptionId = Utils.getSettings().getPlanetAPISubscriptionId();

        Response result = null;
        try {
            result = client
                    .target(orderAPIv2)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", getAPIToken(demoAccount))
                    .buildPost(Entity.json(orderRequest))
                    .submit()
                    .get();
            if(result.getStatus() >= 300) {
                logger.debug("Payload is " + new ObjectMapper().writeValueAsString(orderRequest));
                throw new Exception("method returned " + result.getStatus());
            }
            //logger.debug(result.readEntity(String.class));
            OrderResponse orderResponse = result.readEntity(OrderResponse.class);
            return orderResponse.id;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EIException("Error requesting planet product #" + sceneId + ", reason is " + e.getMessage());
        }
    }

    public static OrderStatus getOrderV2DownloadUrl(String orderId, boolean demoAccount) throws EIException {
        Client client = ClientBuilder.newClient();

        Response result = null;
        try {
            result = client
                    .target(orderAPIv2 + "/" + orderId)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", getAPIToken(demoAccount))
                    .buildGet()
                    .submit()
                    .get();
            if (result.getStatus() >= 300) {
                throw new Exception("method returned " + result.getStatus());
            }
            OrderStatus orderStatus = result.readEntity(OrderStatus.class);
            return orderStatus;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EIException("Error fetching order status with URL " + orderAPIv2 + ", reason is " + e.getMessage());
        }
    }

    public static PLATFORM getPlatform(String sceneId, boolean demoAccount) throws EIException {
        for(PLATFORM platform : new PLATFORM[] {PLATFORM.PSScene4Band, PLATFORM.PSScene3Band}) {

            Client client = ClientBuilder.newClient();

            String apiUrl = assetsAPIUrl.replace("$platform", platform.toString())
                    .replace("$id", sceneId);

            Response result = null;
            try {
                result = client
                        .target(apiUrl)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Authorization", getAPIToken(demoAccount))
                        .buildGet()
                        .submit()
                        .get();
                if (result.getStatus() <= 300) {
                    logger.info("Platform for scene id " + sceneId + " is " + platform);
                    return platform;
                }
                if (result.getStatus() != 404) {
                    String response = result.readEntity(String.class);
                    logger.debug("Status is " + result.getStatus() + " and payload is " + response);
                    throw new Exception(response);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new EIException("Error calling API, reason is " + e.getMessage());
            }
        }
        return null;
    }

    public static AccountValues getAccountValues() throws EIException {
        Client client = ClientBuilder.newClient();

        Response result = null;
        try {
            result = client
                    .target(accountAPIUrl)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("Authorization", getAPIToken(false))
                    .buildGet()
                    .submit()
                    .get();
            if (result.getStatus() <= 300) {
                return result.readEntity(new GenericType<List<AccountValues>>(){}).get(0);
            }
            throw new Exception(result.getEntity().toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new EIException("Error calling API, reason is " + e.getMessage());
        }
    }

    public static String getAPIToken(boolean demoAccount) {
        return "api-key " +
                (demoAccount ? Configuration.getProperty(Configuration.APPLICATION_SETTINGS.planetAPIToken) : Utils.getSettings().getPlanetAPIToken());
    }

}
