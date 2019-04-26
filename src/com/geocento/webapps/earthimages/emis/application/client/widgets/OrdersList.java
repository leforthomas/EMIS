package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.CellListResource;
import com.geocento.webapps.earthimages.emis.application.share.EventSummaryDTO;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.metaaps.webapps.libraries.client.widget.CustomPager;

import java.util.ArrayList;
import java.util.List;

public class OrdersList extends Composite implements RequiresResize {

    static public interface Presenter extends OrderSummaryCell.Presenter {
        void handleSelection(EventSummaryDTO eventSummaryDTO);
    }

    private CellList<EventSummaryDTO> cellList;

    protected ListDataProvider<EventSummaryDTO> dataProvider;
    private SingleSelectionModel<EventSummaryDTO> selectionModel;
    private SimplePager pager;

    public OrdersList(final Presenter presenter) {

        HTMLPanel panel = new HTMLPanel("");

        CellListResource.INSTANCE.cellListStyle().ensureInjected();

        cellList = new CellList<EventSummaryDTO>(new OrderSummaryCell(presenter), CellListResource.INSTANCE);
        cellList.setPageSize(5);
        dataProvider = new ListDataProvider<EventSummaryDTO>();
        dataProvider.setList(new ArrayList<EventSummaryDTO>());
        dataProvider.addDataDisplay(cellList);

        initWidget(panel);

        cellList.setVisible(true);

        panel.add(cellList);

        cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
        // Add a selection model to handle user selection.
        selectionModel = new SingleSelectionModel<EventSummaryDTO>();
        cellList.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                EventSummaryDTO eventSummaryDTO = selectionModel.getSelectedObject();
                if(eventSummaryDTO != null && presenter != null) {
                    presenter.handleSelection(eventSummaryDTO);
                }
            }
        });

        cellList.setWidth("100%");
        cellList.getElement().getFirstChildElement().getStyle().setTextAlign(Style.TextAlign.CENTER);
        cellList.setEmptyListWidget(new Label("No orders..."));

        pager = new CustomPager();
        pager.getElement().getStyle().setProperty("margin", "20px auto");
        //pager.getElement().getStyle().setColor("white");
        pager.setDisplay(cellList);
        panel.add(pager);
	}

    public Widget getFirstElement() {
        return new Widget() {
            {
                //cellList.getRowElement(0).scrollIntoView();
                super.setElement(cellList.getElement().getFirstChildElement().getFirstChildElement());
            }
        };
    }

    public void setOrders(List<EventSummaryDTO> orders) {
        List<EventSummaryDTO> list = dataProvider.getList();
        list.clear();
        list.addAll(orders == null ? new ArrayList<EventSummaryDTO>() : orders);
        dataProvider.refresh();
        pager.setPage(0);
        pager.setVisible(orders.size() > pager.getPageSize());
	}

	public void highlightOrder(final EventSummaryDTO order) {
        selectionModel.setSelected(order, true);
        final int orderIndex = dataProvider.getList().indexOf(order);
        int pageSize = pager.getPageSize();
        pager.setPageStart((int) (Math.floor((double) orderIndex / (double) pageSize)) * pageSize);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {
                int index = cellList.getVisibleItems().indexOf(order);
                if(index != -1) {
                    cellList.getRowElement(index).scrollIntoView();
                }
            }
        });
	}

    public void refresh() {
        dataProvider.refresh();
    }

    public void setLoading(String message) {

    }

    public void clearAll() {
        setOrders(new ArrayList<EventSummaryDTO>());
        refresh();
    }

    @Override
    public void onResize() {
    }

}
