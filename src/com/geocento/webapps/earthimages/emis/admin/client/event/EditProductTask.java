package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 31/07/2017.
 */
public class EditProductTask extends GwtEvent<EditProductTaskHandler> {

    public static Type<EditProductTaskHandler> TYPE = new Type<EditProductTaskHandler>();

    private final ProductFetchTaskDTO productFetchTask;

    public EditProductTask(ProductFetchTaskDTO productFetchTask) {
        this.productFetchTask = productFetchTask;
    }

    public ProductFetchTaskDTO getProductFetchTask() {
        return productFetchTask;
    }

    public Type<EditProductTaskHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(EditProductTaskHandler handler) {
        handler.onEditProductTask(this);
    }
}
