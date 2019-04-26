package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.client.place.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.IconAnchor;

import java.util.Iterator;

/**
 * Created by thomas on 11/01/15.
 */
public class AIApplicationTemplateView extends Composite implements IsWidget, HasWidgets {

    interface AIApplicationTemplateViewUiBinder extends UiBinder<HTMLPanel, AIApplicationTemplateView> {
    }

    private static AIApplicationTemplateViewUiBinder ourUiBinder = GWT.create(AIApplicationTemplateViewUiBinder.class);

    static public interface Style extends CssResource {

    }

    @UiField
    Style style;
    @UiField
    HTMLPanel content;
    @UiField
    HTMLPanel banner;
    @UiField
    Anchor users;
    @UiField
    Anchor sensors;
    @UiField
    Anchor settings;
    @UiField
    IconAnchor logOut;
    @UiField
    Anchor orderingPolicies;
    @UiField
    Anchor logging;
    @UiField
    Anchor publish;
    @UiField
    Anchor orders;
    @UiField
    Anchor samples;

    public AIApplicationTemplateView() {

        initWidget(ourUiBinder.createAndBindUi(this));

        users.setHref("#" + PlaceHistoryHelper.convertPlace(new AIUsersPlace()));
        settings.setHref("#" + PlaceHistoryHelper.convertPlace(new AISettingsPlace()));
        publish.setHref("#" + PlaceHistoryHelper.convertPlace(new AIPublishPlace()));
        orders.setHref("#" + PlaceHistoryHelper.convertPlace(new AIOrdersPlace()));
        samples.setHref("#" + PlaceHistoryHelper.convertPlace(new AISamplesPlace()));
        logging.setHref("#" + PlaceHistoryHelper.convertPlace(new AILogsPlace()));
    }

    @UiChild(tagname="bannerWidget")
    public void addHeaderWidget(Widget widget) {
        banner.add(widget);
    }

    public void setPlace(AIPlace place) {
        if(place instanceof AISettingsPlace) {
            settings.getParent().setStyleName("active");
        } else if(place instanceof AIUsersPlace) {
            users.getParent().setStyleName("active");
        } else if(place instanceof AIPublishPlace) {
            publish.getParent().setStyleName("active");
        } else if(place instanceof AIOrdersPlace) {
            orders.getParent().setStyleName("active");
        } else if(place instanceof AILogsPlace) {
            logging.getParent().setStyleName("active");
        }
    }

    public HasClickHandlers getLogOut() {
        return logOut;
    }

    @Override
    protected void onUnload() {
        super.onUnload();
    }

    @Override
    public void add(Widget w) {
        content.add(w);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return content.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return content.remove(w);
    }

}