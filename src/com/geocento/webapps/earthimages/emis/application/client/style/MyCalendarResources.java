package com.geocento.webapps.earthimages.emis.application.client.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.datepicker.client.CalendarRange;

public interface MyCalendarResources extends Resources {

	public MyCalendarResources INSTANCE =
		GWT.create(MyCalendarResources.class);

	/**
	 * The styles used in this widget.
	 */
	@Source("CalendarRange.css")
	CalendarRange.Style calendarRangeStyle();
}
