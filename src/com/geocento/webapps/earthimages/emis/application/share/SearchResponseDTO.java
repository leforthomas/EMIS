package com.geocento.webapps.earthimages.emis.application.share;

import com.metaaps.webapps.earthimages.extapi.server.domain.SearchResponse;

import java.io.Serializable;
import java.util.List;

public class SearchResponseDTO implements Serializable {

    SearchResponse searchResponse;
    List<ProductOrderDTO> productOrders;

    public SearchResponseDTO() {
    }

    public SearchResponse getSearchResponse() {
        return searchResponse;
    }

    public void setSearchResponse(SearchResponse searchResponse) {
        this.searchResponse = searchResponse;
    }

    public List<ProductOrderDTO> getProductOrders() {
        return productOrders;
    }

    public void setProductOrders(List<ProductOrderDTO> productOrders) {
        this.productOrders = productOrders;
    }
}
