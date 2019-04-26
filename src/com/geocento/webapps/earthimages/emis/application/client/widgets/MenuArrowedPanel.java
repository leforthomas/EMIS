package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.ArrowedWidgetPopupPanel;
import com.metaaps.webapps.libraries.client.widget.IconAnchor;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;

import java.util.Iterator;

public class MenuArrowedPanel extends IconAnchor implements ResizeHandler, HasWidgets {

	public interface Presenter {
		void handleOpen(boolean open);
	}
	
	private ArrowedWidgetPopupPanel optionsPopup;
	
	private TYPE type = TYPE.below;
	
	private Presenter presenter;
	
	public MenuArrowedPanel() {
        setSimple(true);
		if(optionsPopup == null) {
			optionsPopup = new ArrowedWidgetPopupPanel();
			optionsPopup.setAutoHideEnabled(true);
		}
		getElement().getStyle().setCursor(Cursor.POINTER);
		addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				boolean display = !optionsPopup.isShowing();
				displayMenu(display);
				if(presenter != null) {
					presenter.handleOpen(display);
				}
			}
		});
		Window.addResizeHandler(this);
	}
	
	public void setType(TYPE type) {
		this.type = type;
	}

	@Override
	protected void onUnload() {
		super.onUnload();
	}
	
	@Override
	public void onResize(ResizeEvent event) {
		// refresh display of the popup position
		displayMenu(optionsPopup.isShowing());
	}
	
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	public void displayMenu(boolean display) {
		if(display) {
			optionsPopup.showAt(this, type);
		} else {
			optionsPopup.hide();
		}
	}

	@Override
	public void add(Widget w) {
		optionsPopup.add((IsWidget) w);
	}

	@Override
	public void clear() {
		optionsPopup.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return optionsPopup.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return optionsPopup.remove(w);
	}

	public void updatePanel() {
		optionsPopup.onResize(null);
	}

    public void setMaxWidth(String maxWidth) {
        span.getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        span.getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
        span.getStyle().setOverflow(Style.Overflow.HIDDEN);
        span.getStyle().setTextOverflow(Style.TextOverflow.ELLIPSIS);
        span.getStyle().setProperty("maxWidth", maxWidth);
    }

}
