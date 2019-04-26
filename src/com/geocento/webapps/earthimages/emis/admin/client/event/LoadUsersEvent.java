package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 17/06/2015.
 */
public class LoadUsersEvent extends GwtEvent<LoadUsersEventHandler> {

    public static Type<LoadUsersEventHandler> TYPE = new Type<LoadUsersEventHandler>();

    int start;
    int length;
    String sortBy;
    boolean isAscending;

    public LoadUsersEvent(int start, int length, String sortBy, boolean isAscending) {
        this.start = start;
        this.length = length;
        this.sortBy = sortBy;
        this.isAscending = isAscending;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public void setAscending(boolean isAscending) {
        this.isAscending = isAscending;
    }

    public Type<LoadUsersEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(LoadUsersEventHandler handler) {
        handler.onLoadUsers(this);
    }
}
