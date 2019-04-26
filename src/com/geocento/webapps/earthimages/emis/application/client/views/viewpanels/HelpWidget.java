package com.geocento.webapps.earthimages.emis.application.client.views.viewpanels;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.SimplePanel;
import com.metaaps.webapps.libraries.client.widget.ArrowedPopUpMenu;
import com.metaaps.webapps.libraries.client.widget.PopUpMenu.IPopupMenuEventListener;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;

public class HelpWidget extends Anchor {

    static public interface Presenter {
		void startTour();
	}

	private ArrowedPopUpMenu popupMenu = null;
	
	private Presenter presenter;

	private Anchor typeOfImageryAnchor;
	private Anchor faqAnchor;
    private Anchor contactUs;

    private String typeOfImagery;
	private String faqHref;
    private String contactUsHref;

	public HelpWidget() {
		setText("Help");
		Element span = DOM.createSpan();
		span.addClassName("ei-arrowDown");
		span.addClassName("white");
		span.getStyle().setMarginLeft(10, Unit.PX);
        DOM.appendChild(getElement(), span);
		getElement().getStyle().setColor("white");
		getElement().getStyle().setFontSize(1.0, Unit.EM);
		getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
		addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				expand();
			}
		});
	}

	private void createPopupMenu() {
		popupMenu = new ArrowedPopUpMenu();
		popupMenu.getElement().getStyle().setMargin(5, Unit.PX);
        // TODO - add tests to know if these links are active
/*
		faqAnchor = new Anchor("FAQ");
		faqAnchor.setTarget("_blank");
		faqAnchor.setHref(faqHref);
		faqAnchor.getElement().getStyle().setDisplay(Display.BLOCK);
		faqAnchor.setWidth("100%");
		popupMenu.addMenuItem(new SimplePanel(faqAnchor));
        typeOfImageryAnchor = new Anchor("Type of Imagery");
        typeOfImageryAnchor.setTarget("_blank");
        typeOfImageryAnchor.setHref(typeOfImagery);
        typeOfImageryAnchor.getElement().getStyle().setDisplay(Display.BLOCK);
        typeOfImageryAnchor.setWidth("100%");
        popupMenu.addMenuItem(new SimplePanel(typeOfImageryAnchor));
        popupMenu.addMenuDivider();
*/
		if(presenter != null) {
			popupMenu.addMenuItem(new IPopupMenuEventListener() {
				
				@Override
				public void execute() {
					if(HelpWidget.this.presenter != null) {
						HelpWidget.this.presenter.startTour();
					}
				}
			}, "Guided tour");
            popupMenu.addMenuDivider();
		}
        contactUs = new Anchor("Contact Us");
        contactUs.setTarget("_blank");
        contactUs.setHref(contactUsHref);
        contactUs.getElement().getStyle().setDisplay(Display.BLOCK);
        contactUs.setWidth("100%");
        popupMenu.addMenuItem(new SimplePanel(contactUs));
	}
	
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	public void setTypeOfImagery(String url) {
		typeOfImagery = url;
	}

	public void setFAQHref(String url) {
		faqHref = url;
	}

    public void setContactUsHref(String url) {
        contactUsHref = url;
    }

    public void expand() {
		if(popupMenu == null) {
			createPopupMenu();
		}
		popupMenu.showAt(HelpWidget.this, TYPE.below);
	}

}
