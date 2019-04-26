package com.geocento.webapps.earthimages.emis.application.client.activities;

import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.event.LogOut;
import com.geocento.webapps.earthimages.emis.application.client.event.LogOutHandler;
import com.geocento.webapps.earthimages.emis.application.client.event.OrderNotification;
import com.geocento.webapps.earthimages.emis.application.client.event.OrderNotificationHandler;
import com.geocento.webapps.earthimages.emis.application.client.place.ViewEventPlace;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.services.LoginService;
import com.geocento.webapps.earthimages.emis.application.client.utils.HubspotChatHelper;
import com.geocento.webapps.earthimages.emis.application.client.utils.NotificationSocketHelper;
import com.geocento.webapps.earthimages.emis.application.client.views.EILiteTemplateView;
import com.geocento.webapps.earthimages.emis.application.client.widgets.Toast;
import com.geocento.webapps.earthimages.emis.application.share.websockets.ProductOrderNotification;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Created by thomas on 12/01/15.
 */
public abstract class EMISTemplateActivity extends AbstractCustomerActivity {

    private EILiteTemplateView templateView;

    public EMISTemplateActivity(ClientFactory clientFactory) {
        super(clientFactory);
        // add chat to page if not available
        HubspotChatHelper.injectScript();
    }

    public void initialiseTemplate(final EILiteTemplateView templateView) {

        this.templateView = templateView;

        templateView.setEventBus(activityEventBus);

        // start websockets
        NotificationSocketHelper.getInstance().startMaybeNotifications();

        activityEventBus.addHandler(OrderNotification.TYPE, new OrderNotificationHandler() {
            @Override
            public void onOrderNotification(OrderNotification event) {
                handleOrderNotification(event.getProductOrderNotification());
            }
        });

        activityEventBus.addHandler(LogOut.TYPE, new LogOutHandler() {
            @Override
            public void onLogOut(LogOut event) {
                LoginService.App.getInstance().logOut(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {

                    }

                    @Override
                    public void onSuccess(Void result) {
                        com.geocento.webapps.earthimages.emis.application.client.utils.Utils.saveSession(null, true);
                        Window.Location.reload();
                    }
                });
            }
        });
    }

    protected void handleOrderNotification(ProductOrderNotification productOrderNotification) {
        // default is to display a message
        switch(productOrderNotification.status) {
            case Completed: {
                HTMLPanel panel = new HTMLPanel("Product order " + productOrderNotification.productId + " has completed, " +
                        "click <a style='color: white; text-decoration: underline;' href='#" + PlaceHistoryHelper.convertPlace(
                        new ViewEventPlace(com.metaaps.webapps.libraries.client.widget.util.Utils.generateTokens(ViewEventPlace.TOKENS.eventid.toString(), productOrderNotification.eventId))) +
                        "'>here</a> to view product");
                panel.getElement().getStyle().setPadding(20, com.google.gwt.dom.client.Style.Unit.PX);
                panel.getElement().getStyle().setBackgroundColor("green");
                panel.getElement().getStyle().setColor("white");
                Toast.getInstance().display(panel, 30000);
            } break;
            case Failed: {
                HTMLPanel panel = new HTMLPanel("Product order " + productOrderNotification.productId + " has failed, " +
                        "click <a style='color: white; text-decoration: underline;' href='#" + PlaceHistoryHelper.convertPlace(
                        new ViewEventPlace(com.metaaps.webapps.libraries.client.widget.util.Utils.generateTokens(ViewEventPlace.TOKENS.eventid.toString(), productOrderNotification.eventId))) +
                        "'>here</a> to view product");
                panel.getElement().getStyle().setPadding(20, com.google.gwt.dom.client.Style.Unit.PX);
                panel.getElement().getStyle().setBackgroundColor("red");
                panel.getElement().getStyle().setColor("white");
                Toast.getInstance().display(panel, 30000);
            } break;
        }

    }

    protected void displayLoading(String message) {
        templateView.displayLoading(message);
    }

    protected void hideLoading() {
        templateView.hideLoading();
    }

    protected void displayError(String message) {
        templateView.displayError(message);
    }

}
