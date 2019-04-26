package com.geocento.webapps.earthimages.emis.admin.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.metaaps.webapps.libraries.client.widget.ClientFactory;
import com.metaaps.webapps.libraries.client.widget.IconLabel;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;

/**
 * a widget to display and handle an image upload
 * requires a servlet located at module_address/uploadimage
 * 
 * @author thomas
 *
 */
public class UploadProductFormPopup extends PopupPanel implements ResizeHandler {

	private static UploadImageFormPopupUiBinder uiBinder = GWT
			.create(UploadImageFormPopupUiBinder.class);
	
	private static UploadProductFormPopup instance = null;

	private String productOrderId;

    interface UploadImageFormPopupUiBinder extends
			UiBinder<Widget, UploadProductFormPopup> {
	}

	@UiField FlowPanel imageFormPanel;
	@UiField FileUpload uploadThumbnail;
	@UiField FormPanel imageForm;
	@UiField
    IconLabel message;
    @UiField
    TextBox productId;

    private Presenter presenter = null;

	/*
	 * A widget for loading an image to the server
	 * expects a servlet at moduleBaseUrl/uploadimage which can handle imageWidth and imageHeight parameters
	 */
	protected UploadProductFormPopup() {
		
		add(uiBinder.createAndBindUi(this));

		removeStyleName("gwt-PopupPanel");
		addStyleName("simplePopupPanel");
		setAnimationEnabled(true);
		setAutoHideEnabled(false);
		setGlassEnabled(true);
		getElement().getStyle().setZIndex(10000);
		
		imageForm.setEncoding(FormPanel.ENCODING_MULTIPART);
		imageForm.setMethod(FormPanel.METHOD_POST);
		imageForm.setAction(GWT.getModuleBaseURL() + "uploadproduct");

		imageForm.addSubmitHandler(new FormPanel.SubmitHandler() {
            public void onSubmit(SubmitEvent event) {
            	// check file name is not empty
                if (!"".equalsIgnoreCase(uploadThumbnail.getFilename())) {
                	productId.setText(productOrderId + "");
                }
                else{
                    event.cancel(); // cancel the event
                }
            }
	    });
	
		imageForm.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            public void onSubmitComplete(SubmitCompleteEvent event) {
            	String result = event.getResults();
            	if(result == null || result.startsWith("error=")) {
            		displayError("Could not upload product, reason: " + (result == null ? "could not store the image" : result.replaceFirst("error=", "")));
                	if(presenter != null) {
                		presenter.handleImageUploadedError(result);
                	}
            	} else {
                    result = StringUtils.extract(result, "<value>", "</value>");
            		displayMessage(false);
            		hide();
                	if(presenter != null) {
                		presenter.handleProductUploaded(result);
                	}
            	}
            }
	    });
		
		Window.addResizeHandler(this);
		
		displayMessage(false);

	}
	
	public static UploadProductFormPopup getInstance() {
		if(instance == null) {
			instance = new UploadProductFormPopup();
		}
		return instance;
	}
	
	private void displayMessage(boolean display) {
		message.setVisible(display);
	}
	
	private void displayLoading() {
		displayMessage(true);
		message.setResource(ClientFactory.getStyleResources().loading());
		message.setText("uploading image...");
	}

	private void displayError(String message) {
		displayMessage(true);
		this.message.setResource(ClientFactory.getStyleResources().error());
		this.message.setText(message);
	}

	@UiHandler("uploadImage")
	void uploadImage(ClickEvent event) {
		displayLoading();
		if(presenter != null) {
			presenter.handleUploadingStarted();
		}
		imageForm.submit();
	}
	
	@UiHandler("cancelImage")
	void cancelUpload(ClickEvent event) {
		hide();
		if(presenter != null) {
			presenter.handleUploadingCancelled();
		}
	}
	
	public void showUpload(String productOrderId, Presenter presenter) {
	    this.productOrderId = productOrderId;
		this.presenter = presenter;
		displayMessage(false);
		center();
		show();
	}

	public static interface Presenter {
		void handleUploadingCancelled();
		void handleImageUploadedError(String string);
		void handleUploadingStarted();
		void handleProductUploaded(String imageUrl);
	}
	
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void onResize(ResizeEvent event) {
		// refresh display of the popup position
		if(isShowing()) {
			center();
		}
	}

}
