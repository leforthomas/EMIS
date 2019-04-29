package com.geocento.webapps.earthimages.emis.application.share.websockets;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.entities.PUBLICATION_STATUS;
import com.geocento.webapps.earthimages.emis.application.share.ProductMetadataDTO;

import java.util.ArrayList;

/**
 * Created by thomas on 10/05/2017.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ProductOrderNotification {

    public String eventId;
    public String productId;
    public PRODUCTORDER_STATUS status;
    public PUBLICATION_STATUS publishStatus;
    public ArrayList<ProductMetadataDTO> publishedProducts;
    public String productServiceWMSUrl;
    public String thumbnailUrl;
    public String orderId;

    public ProductOrderNotification() {
    }

}
