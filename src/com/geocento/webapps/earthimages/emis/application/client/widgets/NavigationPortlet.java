package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.event.logical.shared.HasShowRangeHandlers;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.metaaps.webapps.libraries.client.widget.NavigationToolbar;

public class NavigationPortlet extends PortletTemplate implements HasShowRangeHandlers<Integer>
{

	protected NavigationToolbar navigationToolbar;

	public NavigationPortlet() {
		super();
	}
	
	public void addNavigationToolbar(int pagesize, int maxcount) {
		navigationToolbar = new NavigationToolbar(toolbar, pagesize, maxcount, this);
	}
	
	public void setPageSize(int pageSize) {
		navigationToolbar.setPageSize(pageSize);
	}
	
	public void setMaxCount(int maxCount) {
		navigationToolbar.setMaxCount(maxCount);
	}
	
	public int getMaxCount() {
		return navigationToolbar.getMaxCount();
	}
	
	public int getPageSize() {
		return navigationToolbar.getPageSize();
	}
	
	public int getIndex() {
		return navigationToolbar.getIndex();
	}

    public void setIndex(int index) {
        navigationToolbar.setIndex(index);
    }

    @Override
	public void setLoading(boolean loading) {
		super.setLoading(loading);
		navigationToolbar.setLoading(loading);
	}
	
	@Override
	public boolean isLoading() {
		return navigationToolbar.isLoading();
	}

	@Override
	public HandlerRegistration addShowRangeHandler(ShowRangeHandler<Integer> handler) {
		return addHandler(handler, ShowRangeEvent.getType());
	}

}
