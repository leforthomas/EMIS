package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.StateSelectedBox.SelectHandler;
import com.metaaps.webapps.libraries.client.widget.util.Utils.SELECTION_STATE;

public class SelectIconWidget<T> extends Composite {

    private static SelectIconWidgetUiBinder uiBinder = GWT
            .create(SelectIconWidgetUiBinder.class);

    interface SelectIconWidgetUiBinder extends
            UiBinder<Widget, SelectIconWidget> {
    }

	protected static StyleResources style = GWT.create(StyleResources.class);

	protected T resource;
    @UiField
    HTMLPanel panel;
    @UiField
    protected StateSelectedBox selectedBox;
    @UiField
    protected
    Image labelIcon;
    @UiField
    protected Label label;

    public SelectIconWidget(T resource) {

        initWidget(uiBinder.createAndBindUi(this));

        this.resource = resource;
	}

	public T getResource() {
		return resource;
	}

    public void setSelectHandler(SelectHandler selectHandler) {
        selectedBox.setSelectHandler(selectHandler);
    }
	
	public void select(boolean selected) {
		this.selectedBox.setSelected(selected);
	}

	public SELECTION_STATE getSelectionState() {
		return selectedBox.getSelectionState();
	}

    public void setSelectionState(SELECTION_STATE selection_state) {
        selectedBox.setSelectionState(selection_state);
    }

	public void setLabel(String label) {
		this.label.setText(label);
	}

    public void setImageResource(ImageResource imageResource) {
        this.labelIcon.setResource(imageResource);
    }

}

