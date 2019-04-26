package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.application.client.utils.Utils;
import com.geocento.webapps.earthimages.emis.application.client.views.viewpanels.HelpWidget;
import com.geocento.webapps.earthimages.emis.application.client.views.viewpanels.UserWidget;
import com.geocento.webapps.earthimages.emis.application.client.widgets.LoadingPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.widget.*;
import com.metaaps.webapps.libraries.client.widget.util.Util;

import java.util.Iterator;

/**
 * Created by thomas on 11/01/15.
 */
public class EILiteTemplateView extends Composite implements IsWidget, HasWidgets {

    interface EILiteTemplateViewUiBinder extends UiBinder<HTMLPanel, EILiteTemplateView> {
    }

    private static EILiteTemplateViewUiBinder ourUiBinder = GWT.create(EILiteTemplateViewUiBinder.class);

    static public interface Style extends CssResource {

    }

    @UiField
    Style style;
    @UiField
    HTMLPanel content;
    @UiField
    HTMLPanel banner;
    @UiField
    UnOrderedList widgets;
    @UiField
    HelpWidget help;
    @UiField
    IconAnchor homeIcon;
    @UiField
    UserWidget userWidget;

    private EventBus eventBus;

    public EILiteTemplateView() {

        initWidget(ourUiBinder.createAndBindUi(this));

        userWidget.setUser(Utils.getLoginInfo().getUserName());
        userWidget.setDisplaySamples(Utils.getLoginInfo().getUserRole() == USER_ROLE.ADMINISTRATOR);

        help.setTypeOfImagery("http://geocento.com/satellite-imagery-gallery");
        help.setFAQHref("http://geocento.com/satellite-imagery-frequently-asked-questions/");
        help.setContactUsHref(Application.getApplicationSettings().getContactUs());
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @UiChild(tagname="bannerWidget")
    public void addHeaderWidget(Widget widget) {
        banner.add(widget);
    }

    @UiChild(tagname="widgets")
    public void addWidgets(Widget widget) {
        widgets.addListItem(widget);
    }

    public void displayAbout(String about) {
        ArrowedPopup popup = ArrowedPopup.getInstance();
        HTMLPanel htmlPanel = new HTMLPanel(about);
        htmlPanel.getElement().getStyle().setBackgroundColor("white");
        htmlPanel.getElement().getStyle().setPadding(20, com.google.gwt.dom.client.Style.Unit.PX);
        popup.showAt(help, htmlPanel, Util.TYPE.below);
    }

    public void displayError(String message) {
        Window.alert(message);
    }

    public void displayLoading(String message) {
        LoadingPanel.getInstance().show(message);
    }

    public void hideLoading() {
        LoadingPanel.getInstance().hide();
    }

    public void setHelpPresenter(HelpWidget.Presenter presenter) {
        help.setPresenter(presenter);
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