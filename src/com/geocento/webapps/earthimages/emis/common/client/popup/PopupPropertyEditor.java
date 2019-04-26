package com.geocento.webapps.earthimages.emis.common.client.popup;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.property.editor.PropertiesEditor;
import com.metaaps.webapps.libraries.client.property.editor.PropertyEditor.LAYOUT;
import com.metaaps.webapps.libraries.client.property.editor.PropertyEditor.MODE;
import com.metaaps.webapps.libraries.client.widget.BasePopup;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;

import java.util.List;

public class PopupPropertyEditor extends Composite {

	private static PopupPropertyEditorUiBinder uiBinder = GWT
			.create(PopupPropertyEditorUiBinder.class);

	interface PopupPropertyEditorUiBinder extends
			UiBinder<Widget, PopupPropertyEditor> {
	}
	
	@UiField
    BasePopup popup;
	@UiField protected HTMLPanel editorsPanel;
	@UiField Label comments;
	@UiField(provided=true) PropertiesEditor propertiesEditor;
	@UiField Anchor done;
	
	private CompletionHandler<List<Property>> completionHandler;
	
	static private PopupPropertyEditor popupPropertyEditor = null;
    
	public PopupPropertyEditor() {
		propertiesEditor = new PropertiesEditor(MODE.EDIT, LAYOUT.BELOW, null);
		initWidget(uiBinder.createAndBindUi(this));
		popup.setModal(true);
        popup.setDraggingEnabled(true);
        bind();
	}

    private void bind() {
        editorsPanel.sinkEvents(Event.ONKEYUP);
        editorsPanel.addDomHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent keyEvent) {
                int keyCode = keyEvent.getNativeEvent().getKeyCode();
                if(keyCode == KeyCodes.KEY_ENTER) {
                    done(null);
                } else if(keyCode == KeyCodes.KEY_ESCAPE) {
                    cancel(null);
                }
            }
        }, KeyUpEvent.getType());
    }

    static public PopupPropertyEditor getInstance() {
		if(popupPropertyEditor == null) {
			popupPropertyEditor = new PopupPropertyEditor();
		}
		return popupPropertyEditor;
	}

	public void editAt(Widget widget, TYPE type, String title, String comments, List<Property> properties, CompletionHandler<List<Property>> completionHandler) {
		this.comments.setText(comments);
		this.comments.setVisible(true);
		displayAt(widget, type, title, properties, completionHandler);
	}
	
	public void edit(String title, String comments, List<Property> properties, CompletionHandler<List<Property>> completionHandler) {
		editAt(null, null, title, comments, properties, completionHandler);
	}
	
	public void editAt(Widget widget, TYPE type, String title, List<Property> properties, CompletionHandler<List<Property>> completionHandler) {
		this.comments.setText("");
		this.comments.setVisible(false);
		displayAt(widget, type, title, properties, completionHandler);
	}
	
	public void edit(String title, List<Property> properties, CompletionHandler<List<Property>> completionHandler) {
		editAt(null, null, title, properties, completionHandler);
	}
	
	public void edit(String title, Property property, CompletionHandler<List<Property>> completionHandler) {
		edit(title, ListUtil.toList(property), completionHandler);
	}
	
	private void displayAt(Widget widget, TYPE type, String title, List<Property> properties, CompletionHandler<List<Property>> completionHandler) {
		popup.setTitleText(title);
		propertiesEditor.setProperties(properties);
		propertiesEditor.setFocus();
		this.completionHandler = completionHandler;
		popup.showAt(widget, type);
	}

	@UiHandler("cancel")
	void cancel(ClickEvent event) {
		if(completionHandler != null) {
			completionHandler.onCancel();
		}
		popup.hide();
	}
	
	@UiHandler("done")
	void done(ClickEvent event) {
		try {
			propertiesEditor.validateProperties();
		} catch (ValidationException e) {
		}
		try {
			if(completionHandler != null) {
				completionHandler.onCompleted(propertiesEditor.getProperties());
			}
			popup.hide();
		} catch (ValidationException e) {
		}
	}

	public void editAt(Widget widget, TYPE type, String title, String comments, Property property, CompletionHandler<List<Property>> completionHandler) {
		editAt(widget, type, title, comments, ListUtil.toList(property), completionHandler);
	}
	
	public void editAt(Widget widget, TYPE type, String title, Property property, CompletionHandler<List<Property>> completionHandler) {
		editAt(widget, type, title, ListUtil.toList(property), completionHandler);
	}

	public void hide() {
		popup.hide();
	}

	public void show() {
		popup.show();
	}

}
