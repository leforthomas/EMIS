package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.metaaps.webapps.libraries.client.widget.util.Utils.SELECTION_STATE;

public class StateSelectedBox extends Image {

	private StyleResources styleResources = GWT.create(StyleResources.class);

    static public interface SelectHandler {

        void onSelected(boolean selected);

    }

    private com.metaaps.webapps.libraries.client.widget.StateSelectedBox.SelectHandler selectHandler;

    private SELECTION_STATE state;

    public StateSelectedBox() {
        super();
        setSelectionState(SELECTION_STATE.SELECTED);
        addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                boolean selected = true;
                switch(getSelectionState()) {
                    case PARTIALLY_SELECTED:
                    case UNSELECTED:
                        setSelectionState(SELECTION_STATE.SELECTED);
                        selected = true;
                        break;
                    case SELECTED:
                        setSelectionState(SELECTION_STATE.UNSELECTED);
                        selected = false;
                        break;
                }
                if(selectHandler != null) {
                    selectHandler.onSelected(selected);
                }
            }
        });
        getElement().getStyle().setCursor(Style.Cursor.POINTER);
    }

    public void setSelectHandler(com.metaaps.webapps.libraries.client.widget.StateSelectedBox.SelectHandler selectHandler) {
        this.selectHandler = selectHandler;
    }

    public SELECTION_STATE getSelectionState() {
        return state;
    }

    public void setSelected(boolean selected) {
        setSelectionState(selected ? SELECTION_STATE.SELECTED : SELECTION_STATE.UNSELECTED);
    }

	public void setSelectionState(SELECTION_STATE state) {
        this.state = state;
		switch(state) {
		case PARTIALLY_SELECTED:
			setResource(styleResources.partialCheckedWhite());
			break;
		case SELECTED:
			setResource(styleResources.checkedWhite());
			break;
		case UNSELECTED:
			setResource(styleResources.uncheckedWhite());
			break;
		}
	}

}
