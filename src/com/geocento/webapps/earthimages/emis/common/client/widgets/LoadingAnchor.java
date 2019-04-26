package com.geocento.webapps.earthimages.emis.common.client.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.metaaps.webapps.libraries.client.widget.IconAnchor;
import com.metaaps.webapps.libraries.client.widget.style.StyleResources;

public class LoadingAnchor extends IconAnchor {

    private boolean loading;

    public LoadingAnchor() {
        setResource(StyleResources.INSTANCE.loading());
        setLoading(false);
        setSimple(true);
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        image.setVisible(loading);
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return super.addClickHandler(event -> {
            if(!loading) {
                handler.onClick(event);
            }
        });
    }
}
