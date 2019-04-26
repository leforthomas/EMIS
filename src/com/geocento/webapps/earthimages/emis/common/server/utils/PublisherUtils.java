package com.geocento.webapps.earthimages.emis.common.server.utils;

import com.geocento.webapps.earthimages.emis.common.server.Configuration;
import com.geocento.webapps.earthimages.emis.common.server.domain.ProductOrder;
import com.geocento.webapps.earthimages.emis.common.server.publishapi.ProductPublisherAPIUtil;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by thomas on 16/11/2017.
 */
public class PublisherUtils {

    static private KeyGenerator keyGenerator = new KeyGenerator(16);

    static private Logger logger = Logger.getLogger(PublisherUtils.class);

    public static String generateThumbnail(ProductOrder productOrder) throws Exception {
        if (productOrder.getPublishProductRequests().size() > 0) {
            Long publishTaskId = productOrder.getPublishProductRequests().get(0).getPublishTaskId();
            return generateThumbnail(publishTaskId);
        } else {
            return null;
        }
    }

    public static String generateThumbnail(Long publishTaskId) throws Exception {
        String imagePath = keyGenerator.CreateKey() + ".jpeg";
        File thumbnailFile = new File(Configuration.getProperty(Configuration.APPLICATION_SETTINGS.baseThumbnailsDirectory) + imagePath);
        try {
            ProductPublisherAPIUtil.getProductThumbnail(publishTaskId, 200, 200, thumbnailFile);
            return "./thumbnails/" + imagePath;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
