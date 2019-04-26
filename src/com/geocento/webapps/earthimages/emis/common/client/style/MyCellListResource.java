package com.geocento.webapps.earthimages.emis.common.client.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellList;

public interface MyCellListResource extends CellList.Resources {

    public static MyCellListResource INSTANCE = GWT.create(MyCellListResource.class);

    interface MyCellListStyle extends CellList.Style {}

    @Override
    @Source({CellList.Style.DEFAULT_CSS, "CellList.css"})
    MyCellListStyle cellListStyle();

}