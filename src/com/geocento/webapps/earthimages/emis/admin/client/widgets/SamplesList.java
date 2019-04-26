package com.geocento.webapps.earthimages.emis.admin.client.widgets;

import com.geocento.webapps.earthimages.emis.admin.client.Admin;
import com.geocento.webapps.earthimages.emis.admin.client.event.DeleteSample;
import com.geocento.webapps.earthimages.emis.admin.client.event.EditSample;
import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;

/**
 * Created by thomas on 21/06/2016.
 */
public class SamplesList extends AsyncPagingCellTable<SampleDTO> {

    private static NumberFormat format = NumberFormat.getFormat("#.##");

    public SamplesList() {
    }

    private Column<SampleDTO, String> editColumn;
    private TextColumn<SampleDTO> nameColumn;
    private TextColumn<SampleDTO> descriptionColumn;

    @Override
    public void initTableColumns(CellTable<SampleDTO> dataGrid) {

        Column<SampleDTO, String> productColumn = new Column<SampleDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(SampleDTO object) {
                return object.getId() + "";
            }
        };
        addResizableColumn(productColumn, "Sample ID", "100px");
        setSortable(productColumn, "sampleId");

        Column<SampleDTO, String> productOrderColumn = new Column<SampleDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(SampleDTO object) {
                return object.getProductOrderDTO().getId() + "";
            }
        };
        productOrderColumn.setFieldUpdater(new FieldUpdater<SampleDTO, String>() {
            @Override
            public void update(int index, SampleDTO sampleDTO, String value) {
                Window.open(GWT.getHostPageBaseURL() + "#order:orderid=" + sampleDTO.getProductOrderDTO().getOrderId(), "order", null);
            }
        });
        addResizableColumn(productOrderColumn, "Product Order ID", "100px");
        setSortable(productOrderColumn, "productOrderId");

        nameColumn = new TextColumn<SampleDTO>() {
            @Override
            public String getValue(SampleDTO object) {
                return object.getName();
            }
        };
        addResizableColumn(nameColumn, "Title", "100px");
        setSortable(nameColumn, "sampleName");

        descriptionColumn = new TextColumn<SampleDTO>() {
            @Override
            public String getValue(SampleDTO object) {
                return object.getDescription() == null ? "Undefined" : object.getDescription();
            }
        };
        addResizableColumn(descriptionColumn, "Description", "200px");

        editColumn = new Column<SampleDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(SampleDTO object) {
                return "Edit";
            }
        };
        addColumn(editColumn, "Action", "100px");
        editColumn.setFieldUpdater(new FieldUpdater<SampleDTO, String>() {

            @Override
            public void update(int index, final SampleDTO sampleDTO, String value) {
                Admin.clientFactory.getEventBus().fireEvent(new EditSample(sampleDTO));
            }
        });

        Column viewColumn = new Column<SampleDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(SampleDTO object) {
                return "View";
            }
        };
        addColumn(viewColumn, "Action", "100px");
        viewColumn.setFieldUpdater((FieldUpdater<SampleDTO, String>) (index, sampleDTO, value) -> Window.open(GWT.getHostPageBaseURL() + "#samples:sampleId=" + sampleDTO.getId(), "samples", null));

        Column deleteColumn = new Column<SampleDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(SampleDTO object) {
                return "Delete";
            }
        };
        addColumn(deleteColumn, "Action", "100px");
        deleteColumn.setFieldUpdater((FieldUpdater<SampleDTO, String>) (index, sampleDTO, value) -> Admin.clientFactory.getEventBus().fireEvent(new DeleteSample(sampleDTO)));

    }

}
