package com.geocento.webapps.earthimages.emis.admin.client.widgets;

import com.geocento.webapps.earthimages.emis.admin.client.Admin;
import com.geocento.webapps.earthimages.emis.admin.client.event.EditProductTask;
import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.metaaps.webapps.libraries.client.widget.ArrowedPopup;
import com.metaaps.webapps.libraries.client.widget.util.Util;

/**
 * Created by thomas on 21/06/2016.
 */
public class ProductFetchTaskList extends AsyncPagingCellTable<ProductFetchTaskDTO> {

    private static NumberFormat format = NumberFormat.getFormat("#.##");

    public ProductFetchTaskList() {
    }

    private Column<ProductFetchTaskDTO, String> editColumn;
    private TextColumn<ProductFetchTaskDTO> nameColumn;
    private TextColumn<ProductFetchTaskDTO> statusColumn;

    @Override
    public void initTableColumns(CellTable<ProductFetchTaskDTO> dataGrid) {

        nameColumn = new TextColumn<ProductFetchTaskDTO>() {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return "Task " + object.getId();
            }
        };
        addResizableColumn(nameColumn, "Task ID", "100px");
        setSortable(nameColumn, "taskId");

        Column<ProductFetchTaskDTO, String> productColumn = new Column<ProductFetchTaskDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getProductOrderId() + "";
            }
        };
        productColumn.setFieldUpdater(new FieldUpdater<ProductFetchTaskDTO, String>() {
            @Override
            public void update(int index, ProductFetchTaskDTO object, String value) {
                Window.alert("TODO - open order page");
            }
        });
        addResizableColumn(productColumn, "Product Order ID", "100px");
        setSortable(productColumn, "productOrderId");

        Column<ProductFetchTaskDTO, String> orderColumn = new Column<ProductFetchTaskDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getOrderId() + "";
            }
        };
        orderColumn.setFieldUpdater(new FieldUpdater<ProductFetchTaskDTO, String>() {
            @Override
            public void update(int index, ProductFetchTaskDTO productFetchTaskDTO, String value) {
                Window.open(GWT.getHostPageBaseURL() + "#order:orderid=" + productFetchTaskDTO.getOrderId(), "order", null);
            }
        });
        addResizableColumn(orderColumn, "Order ID", "100px");
        setSortable(orderColumn, "orderId");

        Column<ProductFetchTaskDTO, String> downloadTaskColumn = new Column<ProductFetchTaskDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getDownloadTaskId() == null ? "NA" : object.getDownloadTaskId() + "";
            }
        };
        downloadTaskColumn.setFieldUpdater(new FieldUpdater<ProductFetchTaskDTO, String>() {
            @Override
            public void update(int index, ProductFetchTaskDTO productFetchTaskDTO, String value) {
                Window.open("./api/product-services/product-downloader/" + productFetchTaskDTO.getPublishTaskId(), "_blank", null);
            }
        });
        addResizableColumn(downloadTaskColumn, "Download task id", "100px");

        Column<ProductFetchTaskDTO, String> publishTaskColumn = new Column<ProductFetchTaskDTO, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getPublishTaskId() == null ? "NA" : object.getPublishTaskId() + "";
            }
        };
        publishTaskColumn.setFieldUpdater(new FieldUpdater<ProductFetchTaskDTO, String>() {
            @Override
            public void update(int index, ProductFetchTaskDTO productFetchTaskDTO, String value) {
                Window.open("./api/product-services/product-publisher/" + productFetchTaskDTO.getPublishTaskId(), "_blank", null);
            }
        });
        addResizableColumn(publishTaskColumn, "Publish task id", "100px");
        setSortable(publishTaskColumn, "publishTaskId");

        statusColumn = new TextColumn<ProductFetchTaskDTO>() {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getStatus() == null ? "Undefined" : object.getStatus().toString();
            }
        };
        addResizableColumn(statusColumn, "Status", "100px");
        setSortable(statusColumn, "status");

        TextColumn<ProductFetchTaskDTO> messageColumn = new TextColumn<ProductFetchTaskDTO>() {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getStatusMessage();
            }
        };
        addResizableColumn(messageColumn, "Message", "100px");
        setSortable(messageColumn, "message");

        TextColumn<ProductFetchTaskDTO> dateColumn = new TextColumn<ProductFetchTaskDTO>() {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getFetchDate() == null ? "NA" : object.getFetchDate().toString();
            }
        };
        addResizableColumn(dateColumn, "Fetch date", "100px");
        setSortable(dateColumn, "fetchDate");

        TextColumn<ProductFetchTaskDTO> completedColumn = new TextColumn<ProductFetchTaskDTO>() {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return object.getCompleted() == null ? "NA" : object.getCompleted().toString();
            }
        };
        addResizableColumn(completedColumn, "Completed date", "100px");
        //setSortable(dateColumn, "fetchDate");

        editColumn = new Column<ProductFetchTaskDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return "Edit";
            }
        };
        addColumn(editColumn, "Action", "100px");
        editColumn.setFieldUpdater(new FieldUpdater<ProductFetchTaskDTO, String>() {

            @Override
            public void update(int index, final ProductFetchTaskDTO productFetchTask, String value) {
                Admin.clientFactory.getEventBus().fireEvent(new EditProductTask(productFetchTask));
            }
        });

        Column viewColumn = new Column<ProductFetchTaskDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(ProductFetchTaskDTO object) {
                return "View";
            }
        };
        addColumn(viewColumn, "Action", "100px");
        viewColumn.setFieldUpdater(new FieldUpdater<ProductFetchTaskDTO, String>() {

            @Override
            public void update(int index, final ProductFetchTaskDTO productFetchTaskDTO, String value) {
                ArrowedPopup popup = ArrowedPopup.getInstance();
                popup.clear();
                HTMLPanel panel = new HTMLPanel("<p>View main values for the task " + productFetchTaskDTO.getId() + "</p>");
                addProperty(panel, "Download task id", productFetchTaskDTO.getDownloadTaskId());
                addProperty(panel, "Publish task id", productFetchTaskDTO.getPublishTaskId());
                popup.add(panel);
                popup.showCentered(Util.TYPE.top, 100);
            }

            private void addProperty(HTMLPanel panel, String name, Object value) {
                panel.add(new HTMLPanel("<b>" + name + "</b>  " + (value == null ? "NA" : value.toString())));
            }
        });

    }

}
