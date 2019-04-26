package com.geocento.webapps.earthimages.emis.admin.client.widgets;

import com.geocento.webapps.earthimages.emis.admin.client.Admin;
import com.geocento.webapps.earthimages.emis.admin.client.event.ChangeOrder;
import com.geocento.webapps.earthimages.emis.admin.client.event.EditProductOrder;
import com.geocento.webapps.earthimages.emis.admin.client.event.MakeSample;
import com.geocento.webapps.earthimages.emis.admin.client.event.UploadProduct;
import com.geocento.webapps.earthimages.emis.admin.share.ProductOrderDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

/**
 * Created by thomas on 21/06/2016.
 */
public class OrdersList extends AsyncPagingCellTable<ProductOrderDTO> {

    private static NumberFormat format = NumberFormat.getFormat("#.##");

    private Column<ProductOrderDTO, String> editColumn;
    private TextColumn<ProductOrderDTO> nameColumn;
    private TextColumn<ProductOrderDTO> statusColumn;

    public OrdersList() {
        super();
    }

    @Override
    public void initTableColumns(CellTable<ProductOrderDTO> dataGrid) {

        Column<ProductOrderDTO, String> productColumn = new Column<ProductOrderDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ProductOrderDTO object) {
                return object.getId() + "";
            }
        };
        productColumn.setFieldUpdater(new FieldUpdater<ProductOrderDTO, String>() {
            @Override
            public void update(int index, ProductOrderDTO object, String value) {
                Window.alert("TODO - open order page");
            }
        });
        addColumn(productColumn, "Product Order ID", "100px");

        Column<ProductOrderDTO, String> orderColumn = new Column<ProductOrderDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ProductOrderDTO object) {
                return object.getOrderId() + "";
            }
        };
        orderColumn.setFieldUpdater(new FieldUpdater<ProductOrderDTO, String>() {
            @Override
            public void update(int index, ProductOrderDTO productOrderDTO, String value) {
                Window.open(GWT.getHostPageBaseURL() + "#order:orderid=" + productOrderDTO.getOrderId(), "order", null);
            }
        });
        addResizableColumn(orderColumn, "Order ID", "100px");
        setSortable(orderColumn, "orderId");

        nameColumn = new TextColumn<ProductOrderDTO>() {
            @Override
            public String getValue(ProductOrderDTO object) {
                return object.getDescription();
            }
        };
        addResizableColumn(nameColumn, "Description", "100px");
        setSortable(nameColumn, "orderName");

        statusColumn = new TextColumn<ProductOrderDTO>() {
            @Override
            public String getValue(ProductOrderDTO object) {
                return object.getStatus() == null ? "Undefined" : object.getStatus().toString();
            }
        };
        addResizableColumn(statusColumn, "Status", "100px");
        setSortable(statusColumn, "status");

        Column createdColumn = new TextColumn<ProductOrderDTO>() {
            @Override
            public String getValue(ProductOrderDTO object) {
                return object.getCreated() == null ? "Undefined" : DateUtil.displaySimpleUTCDate(object.getCreated());
            }
        };
        addResizableColumn(createdColumn, "Created on", "100px");
        setSortable(createdColumn, "createdOn");

        editColumn = new Column<ProductOrderDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(ProductOrderDTO object) {
                return "Edit";
            }
        };
        addColumn(editColumn, "Action", "100px");
        editColumn.setFieldUpdater(new FieldUpdater<ProductOrderDTO, String>() {

            @Override
            public void update(int index, final ProductOrderDTO productOrder, String value) {
                Admin.clientFactory.getEventBus().fireEvent(new EditProductOrder(productOrder));
            }
        });

        Column changeColumn = new Column<ProductOrderDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(ProductOrderDTO object) {
                return "Add to order";
            }
        };
        addColumn(changeColumn, "Action", "100px");
        changeColumn.setFieldUpdater(new FieldUpdater<ProductOrderDTO, String>() {

            @Override
            public void update(int index, final ProductOrderDTO productOrderDTO, String value) {
                Admin.clientFactory.getEventBus().fireEvent(new ChangeOrder(productOrderDTO));
            }
        });

        Column downloadColumn = new Column<ProductOrderDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(ProductOrderDTO productOrderDTO) {
                return isProductDownloaded(productOrderDTO) ? "Download" : "Upload";
            }
        };
        addColumn(downloadColumn, "Action", "100px");
        downloadColumn.setFieldUpdater(new FieldUpdater<ProductOrderDTO, String>() {

            @Override
            public void update(int index, final ProductOrderDTO productOrderDTO, String value) {
                if(isProductDownloaded(productOrderDTO)) {
                    Window.open("./api/download-product/download/" + productOrderDTO.getId() + "/selection/kml", "_blank", null);
                } else {
                    Admin.clientFactory.getEventBus().fireEvent(new UploadProduct(productOrderDTO));
                }
            }
        });

        Column makeSampleColumn = new Column<ProductOrderDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(ProductOrderDTO object) {
                return "Make sample";
            }
        };
        addColumn(makeSampleColumn, "Action", "100px");
        makeSampleColumn.setFieldUpdater(new FieldUpdater<ProductOrderDTO, String>() {

            @Override
            public void update(int index, final ProductOrderDTO productOrderDTO, String value) {
                Admin.clientFactory.getEventBus().fireEvent(new MakeSample(productOrderDTO));
            }
        });
    }

    private boolean isProductDownloaded(ProductOrderDTO productOrderDTO) {
        return ListUtil.toList(new PRODUCTORDER_STATUS[] {PRODUCTORDER_STATUS.Completed}).contains(productOrderDTO.getStatus());
    }

}
