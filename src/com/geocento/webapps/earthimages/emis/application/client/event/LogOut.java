package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 12/01/15.
 */
public class LogOut extends GwtEvent<LogOutHandler> {
    public static Type<LogOutHandler> TYPE = new Type<LogOutHandler>();

    public Type<LogOutHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(LogOutHandler handler) {
        handler.onLogOut(this);
    }
}
