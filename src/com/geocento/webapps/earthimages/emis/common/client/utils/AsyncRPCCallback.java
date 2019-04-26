package com.geocento.webapps.earthimages.emis.common.client.utils;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by thomas on 08/09/2016.
 */
abstract public class AsyncRPCCallback<T extends Object> implements AsyncCallback<T> {

    @Override
    public void onFailure(Throwable caught) {

    }
}
