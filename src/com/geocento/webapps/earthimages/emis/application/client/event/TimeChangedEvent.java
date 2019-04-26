package com.geocento.webapps.earthimages.emis.application.client.event;

import com.google.gwt.event.shared.GwtEvent;

import java.util.Date;

public class TimeChangedEvent extends GwtEvent<TimeChangedEventHandler> {

    public static Type<TimeChangedEventHandler> TYPE = new Type<TimeChangedEventHandler>();

    private Date date;

    public TimeChangedEvent(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public Type<TimeChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(TimeChangedEventHandler handler) {
        handler.onTimeChanged(this);
    }
}
