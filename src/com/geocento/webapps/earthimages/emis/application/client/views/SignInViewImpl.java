package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.client.views.RegisterTemplateView;
import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.GlassPanel;
import com.metaaps.webapps.libraries.client.widget.MessageLabel;

public class SignInViewImpl extends Composite implements SignInView {

	private static AISignInViewUiBinder uiBinder = GWT
			.create(AISignInViewUiBinder.class);

	interface AISignInViewUiBinder extends UiBinder<Widget, SignInViewImpl> {
	}
	
	static private StyleResources styles = GWT.create(StyleResources.class);
	
	public interface Style extends CssResource {
	}

	@UiField Style style;
	
	@UiField
    RegisterTemplateView templateView;
	
	@UiField Panel signInPanel;
	
	@UiField TextBox userName;
	
	@UiField CheckBox keepSignedIn;
	
	@UiField
    MessageLabel message;
	
	@UiField PasswordTextBox password;
	
	@UiField Anchor signInButton;

    private ClientFactory clientFactory;

	private Presenter presenter;

	private boolean locked;
	
	public SignInViewImpl(ClientFactory clientFactory) {
		
		this.clientFactory = clientFactory;
		
		initWidget(uiBinder.createAndBindUi(this));

	}

	private void setLoading(Widget widget) {
		clearWidget(widget);
		widget.addStyleName("ei-loading");
	}

	private void setValidated(Widget widget) {
		clearWidget(widget);
		widget.addStyleName("ei-validated");
	}

	private void setError(Widget widget, String message) {
		clearWidget(widget);
		widget.addStyleName("ei-error");
	}

	private void clearWidget(Widget widget) {
		widget.removeStyleName("ei-loading");
		widget.removeStyleName("ei-validated");
		widget.removeStyleName("ei-error");
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void resetPanels() {
		userName.setText("");
		hideMessage();
		password.setText("");
	}
	
	@Override
	public void displayLoading(String message) {
		this.message.setVisible(true);
		this.message.displayLoading(message);
	}

	@Override
	public void displayErrorMessage(String message) {
		this.message.setVisible(true);
		this.message.displayError(message);
	}

	@Override
	public void hideMessage() {
		this.message.setVisible(false);
	}

	@Override
	public HasText getPassword() {
		return password;
	}

    @Override
	public HasText getUserName() {
		return userName;
	}

	@Override
	public HasClickHandlers getSignInButton() {
		return signInButton;
	}

	@Override
	public HasKeyPressHandlers getPasswordBox() {
		return password;
	}
	
	@Override
	public void lockLinks(boolean locked) {
		this.locked = locked;
		// add glass frame
		if(locked) {
			GlassPanel.getInstance().show();
		} else {
			GlassPanel.getInstance().hide();
		}			
	}

	@Override
	public boolean withSaveSession() {
		return keepSignedIn.getValue();
	}

}
