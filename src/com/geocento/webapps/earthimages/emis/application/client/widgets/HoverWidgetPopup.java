package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.ArrowedWidgetPopupPanel;
import com.metaaps.webapps.libraries.client.widget.util.Activity;
import com.metaaps.webapps.libraries.client.widget.util.ActivityCloseHandler;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;

public abstract class HoverWidgetPopup<T extends Widget, O extends Object> extends ArrowedWidgetPopupPanel implements ActivityCloseHandler {

	protected T widget;
	
	private Timer timer;

	private int hoverTime = 0;

    public HoverWidgetPopup(T widget) {
		super();
		this.widget = widget;
		add((IsWidget) widget);
		setAnimationEnabled(true);
		setAutoHideEnabled(true);
		
		sinkEvents(Event.ONMOUSEOVER);
    	addDomHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				stopTimer();
			}
		}, MouseOverEvent.getType());
		sinkEvents(Event.ONMOUSEOUT);
    	addDomHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				hide();
			}
		}, MouseOutEvent.getType());
    	
		Activity.addCloseHandler(this);
	}
	
	protected void stopTimer() {
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	protected void startTimer() {
		if(hoverTime == 0) {
			hide();
		} else {
			timer = new Timer() {
				
				@Override
				public void run() {
					HoverWidgetPopup.this.hide();
					timer = null;
				}
			};
			timer.schedule(hoverTime);
		}
	}

	abstract protected void onDisplayWidget(O value);

	@Override
	public void showAt(Widget parentWidget, TYPE type) {
		super.showAt(parentWidget, type);
	}

    public void registerPopup(final Widget widget, final O value, final TYPE type, int hoverTime) {
		this.hoverTime = hoverTime;
		widget.sinkEvents(Event.ONMOUSEOVER);
    	widget.addDomHandler(new MouseOverHandler() {

            @Override
			public void onMouseOver(MouseOverEvent event) {
				stopTimer();
				onDisplayWidget(value);
				HoverWidgetPopup.this.showAt(widget, type);
			}
		}, MouseOverEvent.getType());
		widget.sinkEvents(Event.ONMOUSEOUT);
    	widget.addDomHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				startTimer();
			}
		}, MouseOutEvent.getType());
	}
	
	@Override
	public void onActivityClose() {
		hide();
	}

}
