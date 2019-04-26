package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 13/10/2017.
 */
public class ProductOrderChanged extends GwtEvent<ProductOrderChangedHandler> {

    public static Type<ProductOrderChangedHandler> TYPE = new Type<ProductOrderChangedHandler>();

    private ProductOrderDTO productOrderDTO;

    public ProductOrderChanged(ProductOrderDTO productOrderDTO) {
        this.productOrderDTO = productOrderDTO;
    }

    public ProductOrderDTO getProductOrderDTO() {
        return productOrderDTO;
    }

    public Type<ProductOrderChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ProductOrderChangedHandler handler) {
        handler.onProductOrderChanged(this);
    }


}
