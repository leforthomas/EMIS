package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 13/10/2017.
 */
public class EditProductOrder extends GwtEvent<EditProductOrderHandler> {

    public static Type<EditProductOrderHandler> TYPE = new Type<EditProductOrderHandler>();

    private ProductOrderDTO productOrderDTO;

    public EditProductOrder(ProductOrderDTO productOrderDTO) {
        this.productOrderDTO = productOrderDTO;
    }

    public ProductOrderDTO getProductOrderDTO() {
        return productOrderDTO;
    }

    public Type<EditProductOrderHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(EditProductOrderHandler handler) {
        handler.onEditProductOrder(this);
    }
}
