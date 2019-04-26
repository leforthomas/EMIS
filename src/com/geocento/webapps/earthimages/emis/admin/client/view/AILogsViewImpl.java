package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.place.AILogsPlace;
import com.geocento.webapps.earthimages.emis.common.client.popup.LoadingPanel;
import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

public class AILogsViewImpl extends Composite implements AILogsView {

	private static AILogsViewImplUiBinder uiBinder = GWT
			.create(AILogsViewImplUiBinder.class);

    interface AILogsViewImplUiBinder extends UiBinder<Widget, AILogsViewImpl> {
	}

	static private StyleResources styles = GWT.create(StyleResources.class);

	public interface Style extends CssResource {
	}

	@UiField
    Style style;

	@UiField
    AIApplicationTemplateView templateView;
    @UiField
    TextArea logfileContent;
    @UiField
    Button refreshLogfile;
    @UiField
    TextBox filter;

    private ClientFactory clientFactory;

	private Presenter presenter;
	
	public AILogsViewImpl(ClientFactory clientFactory) {
		
		this.clientFactory = clientFactory;

        initWidget(uiBinder.createAndBindUi(this));

        templateView.setPlace(new AILogsPlace());

        filter.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                presenter.filterHasChanged();
            }
        });
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public AIApplicationTemplateView getTemplateView() {
        return templateView;
    }

    @Override
    public void displayError(String message) {
        Window.alert(message);
    }

    @Override
    public void displayLoading(String message) {
        LoadingPanel.getInstance().show(message);
    }

    @Override
    public void hideLoading() {
        LoadingPanel.getInstance().hide();
    }

    @Override
    public void displayLogs(String logsText) {
        logfileContent.setText(logsText);
    }

    @Override
    public HasClickHandlers getRefreshButton() {
        return refreshLogfile;
    }

    @Override
    public void displaySuccess(String message) {
        Window.alert(message);
    }

    @Override
    public String getFilter() {
        return filter.getValue();
    }

}
