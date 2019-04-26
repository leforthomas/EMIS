package com.geocento.webapps.earthimages.emis.admin.client.activity;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.place.AISignInPlace;
import com.geocento.webapps.earthimages.emis.common.share.EILoginException;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.widget.util.Activity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAIActivity extends AbstractActivity {

    protected ClientFactory clientFactory;

	protected List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	protected EventBus activityEventBus;
	
    public AbstractAIActivity(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget acceptsOneWidget, com.google.gwt.event.shared.EventBus eventBus) {
		activityEventBus = eventBus;
	}
	
	abstract protected void bind();

	protected void checkException(Throwable caught) {
		if(caught instanceof EILoginException) {
			clientFactory.getPlaceController().goTo(new AISignInPlace());
		}
	}

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
