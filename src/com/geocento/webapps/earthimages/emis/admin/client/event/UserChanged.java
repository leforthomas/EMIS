package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.admin.share.UserDTO;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 21/01/2015.
 */
public class UserChanged extends GwtEvent<UserChangedHandler> {

    public static Type<UserChangedHandler> TYPE = new Type<UserChangedHandler>();

    private final UserDTO userDTO;

    public UserChanged(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public Type<UserChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(UserChangedHandler handler) {
        handler.onUserChanged(this);
    }
}
