package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 10/08/2017.
 */
public class ProductTaskChanged extends GwtEvent<ProductTaskChangedHandler> {

    public static Type<ProductTaskChangedHandler> TYPE = new Type<ProductTaskChangedHandler>();

    private ProductFetchTaskDTO productFetchTaskDTO;

    public ProductTaskChanged(ProductFetchTaskDTO productFetchTaskDTO) {
        this.productFetchTaskDTO = productFetchTaskDTO;
    }

    public ProductFetchTaskDTO getProductFetchTaskDTO() {
        return productFetchTaskDTO;
    }

    public Type<ProductTaskChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ProductTaskChangedHandler handler) {
        handler.onProductTaskChanged(this);
    }
}
