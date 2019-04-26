package com.geocento.webapps.earthimages.emis.common.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.widget.util.Activity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommonActivity extends AbstractActivity {

	protected List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	protected EventBus activityEventBus;

    @Override
    public void start(AcceptsOneWidget acceptsOneWidget, com.google.gwt.event.shared.EventBus eventBus) {
		activityEventBus = eventBus;
	}
	
	abstract protected void bind();

	protected void unbind() {
		
		((ResettableEventBus) activityEventBus).removeHandlers();
		
		for(HandlerRegistration handler : handlers) {
			handler.removeHandler();
		}
		handlers.clear();
		
	}
	
	@Override
	public String mayStop() {
		unbind();
		Activity.triggerActivityClose();
		return super.mayStop();
	}

}
