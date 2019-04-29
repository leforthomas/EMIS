package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.ValidationEditor;
import com.metaaps.webapps.libraries.client.widget.util.Util;

public class PasswordPopup extends Composite {

	static public interface Presenter {
		void onSubmitted(String password);
		void onCancelled();
	}
	
	private static PasswordPopupUiBinder uiBinder = GWT
			.create(PasswordPopupUiBinder.class);

	interface PasswordPopupUiBinder extends
			UiBinder<Widget, PasswordPopup> {
	}

	private static StyleResources styleResources = GWT.create(StyleResources.class);
	
	@UiField PasswordTextBox password;
	@UiField
    ValidationEditor validation;

	private static PasswordPopup instance;

	private BasePopup popup;
	
	private Presenter presenter;
	
	public PasswordPopup() {
		initWidget(uiBinder.createAndBindUi(this));
		popup = new BasePopup();
		popup.setTitleText("Password Check");
		//popup.setTitleImage(styleResources.validate());
		//popup.setTitleBackgroundColor("#fd9f6b");
		popup.setAutoHideEnabled(false);
		popup.setGlassEnabled(true);
		popup.setContent(this);
		popup.getClose().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		password.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				validation.cleanUp();
			}
		});
		
		bind();
	}
	
	static public PasswordPopup getInstance() {
		if(instance == null) {
			instance = new PasswordPopup();
		}
		return instance;
	}
	
	private void bind() {
	}
	
	public void requestPassword(Presenter presenter) {
		requestPasswordAt(null, null, presenter);
	}
	
	public void requestPasswordAt(Widget widget, Util.TYPE type, Presenter presenter) {
		this.presenter = presenter;
		validation.cleanUp();
		password.setValue("");
		// only show if the widget is visible
		if(widget != null && !widget.isVisible()) {
			return;
		}
		if(widget != null) {
			popup.showAt(widget, type);
		} else {
			popup.show();
			popup.center();
		}
	}
	
	public void hide() {
		popup.hide();
	}
	
	public void setErrorMessage(String errorMessage) {
		validation.setErrorMessage(errorMessage);
	}
	
	@UiHandler("validate")
	void okButton(ClickEvent clickEvent) {
		if(presenter != null) {
			presenter.onSubmitted(password.getValue());
		}
	}
	
	@UiHandler("cancel")
	void cancel(ClickEvent clickEvent) {
		if(presenter != null) {
			presenter.onCancelled();
		}
	}
	
}
