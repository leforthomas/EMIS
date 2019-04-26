package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.metaaps.webapps.libraries.client.widget.GlassPanel;
import com.metaaps.webapps.libraries.client.widget.UnOrderedList;

/**
 * Created by thomas on 27/11/2015.
 */
public class WelcomePage extends GlassPanel {

    private static WelcomePage instance;

    interface WelcomePageUiBinder extends UiBinder<HTMLPanel, WelcomePage> {
    }

    private static WelcomePageUiBinder ourUiBinder = GWT.create(WelcomePageUiBinder.class);

    static public interface Style extends CssResource {
        String glassPanel();
    }

    @UiField
    Style style;

    @UiField
    UnOrderedList logos;
    @UiField
    Anchor support;

    protected WelcomePage() {
        HTMLPanel rootElement = ourUiBinder.createAndBindUi(this);
        add(rootElement);
        setAutoHideEnabled(true);
        setGlassStyleName(style.glassPanel());
    }

    static public WelcomePage getInstance() {
        if(instance == null) {
            instance = new WelcomePage();
        }
        return instance;
    }

    @UiHandler("start")
    public void start(ClickEvent clickEvent) {
        hide();
    }

    public void setSupportUrl(String supportUrl) {
        support.setHref(supportUrl);
    }

}