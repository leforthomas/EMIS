package com.geocento.webapps.earthimages.emis.admin.client.activity;


import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.services.LoginService;
import com.geocento.webapps.earthimages.emis.admin.client.view.AIApplicationTemplateView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by thomas on 12/01/15.
 */
public abstract class AIApplicationTemplateActivity extends AbstractAIActivity {

    public AIApplicationTemplateActivity(ClientFactory clientFactory) {
        super(clientFactory);
    }

    public void initialiseTemplate(final AIApplicationTemplateView templateView) {

        templateView.getLogOut().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                    LoginService.App.getInstance().logOut(new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {

                        }

                        @Override
                        public void onSuccess(Void result) {
                            Utils.saveSession(null, true);
                            Window.Location.reload();
                        }
                    });
            }
        });

    }

}
