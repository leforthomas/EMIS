package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.resources.client.ImageResource;

public class ExpandWidget extends com.metaaps.webapps.libraries.client.widget.ExpandWidget {

    public ExpandWidget() {
    }

    public ExpandWidget(ImageResource collapsedImage, ImageResource expandedImage) {
        super();
        setCollapsedImage(collapsedImage);
        setExpandedImage(expandedImage);
    }
}
