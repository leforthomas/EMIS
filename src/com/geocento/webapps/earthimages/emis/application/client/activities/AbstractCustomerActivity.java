package com.geocento.webapps.earthimages.emis.application.client.activities;

import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.widget.util.Activity;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCustomerActivity extends AbstractActivity {

    protected ClientFactory clientFactory;

	protected List<HandlerRegistration> handlers = new ArrayList<HandlerRegistration>();

	protected EventBus activityEventBus;
	
    public AbstractCustomerActivity(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

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

    static public boolean checkFailure(Throwable caught) {
	    caught.printStackTrace();
        //Window.alert(caught.getMessage());
        if(caught instanceof IncompatibleRemoteServiceException || caught instanceof SerializationException) {
            Window.alert("We have made some improvements and need to reload the application!");
            Window.Location.reload();
            return true;
        }
        return false;
    }

    @Override
	public String mayStop() {
		unbind();
		Activity.triggerActivityClose();
		return super.mayStop();
	}

}
