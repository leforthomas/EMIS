package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.TimePicker;
import com.metaaps.webapps.libraries.client.widget.style.StyleResources;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;

import java.util.Date;

public class OrdersFilter extends Composite {

	public static interface Presenter {

		void setFilter(String name, Date start, Date stop, String status);

	}

	static protected StyleResources styleResources = StyleResources.INSTANCE;

    private static OrdersFilterUiBinder uiBinder = GWT
			.create(OrdersFilterUiBinder.class);

	interface OrdersFilterUiBinder extends UiBinder<Widget, OrdersFilter> {
	}

	public interface Style extends CssResource {

	}


	@UiField
	TextBox textBoxName;
	@UiField
	TimePicker startTime;
	@UiField
	TimePicker stopTime;
	@UiField
	ListBox listBoxStatus;
	@UiField
	Anchor setFilter;

	private Presenter presenter = null;

	public OrdersFilter(Presenter presenter) {

		initWidget(uiBinder.createAndBindUi(this));

        // set default values for start and stop
        Date stop = new Date();
		Date start = DateUtil.addMonths(stop, -10);
		startTime.setDate(start);
		stopTime.setDate(stop);

		listBoxStatus.addItem("ALL", "ALL");

		for (ORDER_STATUS status : ORDER_STATUS.values()) {
			listBoxStatus.addItem(status.toString(), status.toString());
		}

		setFilter.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				String name = textBoxName.getText();
				Date start = startTime.getDate();
				Date stop = stopTime.getDate();
				String status = listBoxStatus.getSelectedValue();

				if(start.getTime() > stop.getTime())
				{
					Window.alert("Start date cannot be grater than end date");
					return;
				}

				if(start.getTime() > new Date().getTime())
				{
					Window.alert("Start date cannot be grater than current date");
					return;
				}

				if(presenter!=null)
					presenter.setFilter(name, start, stop, status);
			}
		});

	}

	public String getName() {return textBoxName.getText();}
	public Date getStart() {return startTime.getDate();}
	public Date getStop() {return stopTime.getDate();}
	public String getStatus() {return listBoxStatus.getSelectedValue();}

}
