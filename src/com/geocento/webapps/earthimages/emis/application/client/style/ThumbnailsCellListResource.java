package com.geocento.webapps.earthimages.emis.application.client.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellList;

public interface ThumbnailsCellListResource extends CellList.Resources {

    public static ThumbnailsCellListResource INSTANCE = GWT.create(ThumbnailsCellListResource.class);

    interface MyCellListStyle extends CellList.Style {}

    @Override
    @Source({CellList.Style.DEFAULT_CSS, "ThumbnailList.css"})
    MyCellListStyle cellListStyle();

}
