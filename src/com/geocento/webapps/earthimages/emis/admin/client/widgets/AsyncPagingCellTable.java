package com.geocento.webapps.earthimages.emis.admin.client.widgets;

import com.google.gwt.user.cellview.client.Column;

import java.util.HashMap;

public abstract class AsyncPagingCellTable<T> extends com.metaaps.webapps.libraries.client.widget.AsyncPagingCellTable<T> {

    protected HashMap<Column, String> resizableColumns;

    public AsyncPagingCellTable() {
    }

    public void setSortBy(String sortBy, boolean isAscending) {
        for(Column column : resizableColumns.keySet()) {
            if(resizableColumns.get(column).contentEquals(sortBy)) {
                column.setDefaultSortAscending(isAscending);
                dataGrid.getColumnSortList().clear();
                dataGrid.getColumnSortList().push(column);
                return;
            }
        }
    }

    public String getSortBy() {
        Column sortedColumn = dataGrid.getColumnSortList().get(0).getColumn();
        return resizableColumns.get(sortedColumn);
    }

    public void setSortable(Column column, String sortBy) {
        if(resizableColumns == null) {
            resizableColumns = new HashMap<Column, String>();
        }
        resizableColumns.put(column, sortBy);
        column.setSortable(true);
    }

}
