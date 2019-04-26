package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 16/06/2015.
 */
public class RemoveUserEvent extends GwtEvent<RemoveUserEventHandler> {

    public static Type<RemoveUserEventHandler> TYPE = new Type<RemoveUserEventHandler>();

    private final String userName;

    public RemoveUserEvent(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public Type<RemoveUserEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(RemoveUserEventHandler handler) {
        handler.onRemoveUser(this);
    }
}
