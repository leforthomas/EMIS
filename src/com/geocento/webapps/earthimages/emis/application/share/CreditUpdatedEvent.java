package com.geocento.webapps.earthimages.emis.application.share;

import com.geocento.webapps.earthimages.emis.application.share.websockets.CreditUpdatedNotification;
import com.google.gwt.event.shared.GwtEvent;

public class CreditUpdatedEvent extends GwtEvent<CreditUpdatedEventHandler> {

    public static Type<CreditUpdatedEventHandler> TYPE = new Type<CreditUpdatedEventHandler>();

    private final CreditUpdatedNotification creditUpdatedNotification;

    public CreditUpdatedEvent(CreditUpdatedNotification creditUpdatedNotification) {
        this.creditUpdatedNotification = creditUpdatedNotification;
    }

    public CreditUpdatedNotification getCreditUpdatedNotification() {
        return creditUpdatedNotification;
    }

    public Type<CreditUpdatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(CreditUpdatedEventHandler handler) {
        handler.onCreditUpdated(this);
    }
}
