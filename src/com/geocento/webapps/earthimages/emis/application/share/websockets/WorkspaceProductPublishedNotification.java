package com.geocento.webapps.earthimages.emis.application.share.websockets;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.geocento.webapps.earthimages.emis.common.share.entities.PUBLICATION_STATUS;
import com.geocento.webapps.earthimages.emis.application.share.ProductMetadataDTO;

import java.util.ArrayList;

/**
 * Created by thomas on 10/05/2017.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class WorkspaceProductPublishedNotification {

    public String workspaceId;
    public Long productPublishRequestId;
    public PUBLICATION_STATUS publishStatus;
    public String message;
    public ArrayList<ProductMetadataDTO> publishedProducts;
    public String productServiceWMSUrl;
    public String productThumbnailUrl;

    public WorkspaceProductPublishedNotification() {
    }

}
