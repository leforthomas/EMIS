package com.geocento.webapps.earthimages.emis.application.client.views.viewpanels;

import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.application.client.event.LogOut;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.metaaps.webapps.libraries.client.widget.ArrowedPopUpMenu;
import com.metaaps.webapps.libraries.client.widget.IconLabel;
import com.metaaps.webapps.libraries.client.widget.util.Activity;
import com.metaaps.webapps.libraries.client.widget.util.ActivityCloseHandler;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;

public class UserWidget extends IconLabel implements ActivityCloseHandler {

    static private StyleResources styleResources = GWT.create(StyleResources.class);

    private boolean displaySamples;

    public UserWidget() {

        setText("Guest");
        // add an arrow
        Element arrow = DOM.createSpan();
        arrow.addClassName("ei-arrowDown");
        arrow.addClassName("white");
        arrow.getStyle().setMarginLeft(5, Style.Unit.PX);
        DOM.appendChild(getElement(), arrow);

        addClickHandler(event -> {
            final ArrowedPopUpMenu popupMenu = ArrowedPopUpMenu.getInstance();
            popupMenu.clearItems();
/*
            popupMenu.addMenuItem(() -> EINEO.clientFactory.getPlaceController().goTo(new EventsPlace()), "Your Orders");
            //popupMenu.addMenuDivider();
            popupMenu.addMenuItem(() -> EINEO.clientFactory.getPlaceController().goTo(new WorkspacesPlace()), "Your Workspaces");
            //popupMenu.addMenuDivider();
            if(displaySamples) {
                popupMenu.addMenuItem(() -> EINEO.clientFactory.getPlaceController().goTo(new SamplesPlace()), "Access Samples");
            }
            popupMenu.addMenuDivider();
            popupMenu.addMenuItem(() -> EINEO.clientFactory.getPlaceController().goTo(new SettingsPlace()), "Manage Account");
            popupMenu.addMenuDivider();
*/
            popupMenu.addMenuItem(() -> Application.clientFactory.getEventBus().fireEvent(new LogOut()), "Log out");
            popupMenu.showAt(UserWidget.this, TYPE.below);
        });
        // make sure the panel is hidden when the activity closes
        Activity.addCloseHandler(this);
	}

    public void setUser(String userName) {
        setText(userName);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

    public void setDisplaySamples(boolean displaySamples) {
        this.displaySamples = displaySamples;
    }

    @Override
    public void onActivityClose() {
        ArrowedPopUpMenu.getInstance().hide();
    }
}
