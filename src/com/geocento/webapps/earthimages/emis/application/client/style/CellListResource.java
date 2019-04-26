package com.geocento.webapps.earthimages.emis.application.client.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellList;

public interface CellListResource extends CellList.Resources {

    public static CellListResource INSTANCE = GWT.create(CellListResource.class);

    interface MyCellListStyle extends CellList.Style {}

    @Override
    @Source({CellList.Style.DEFAULT_CSS, "CellList.css"})
    MyCellListStyle cellListStyle();

}
