package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.application.client.event.AddAoIImported;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;
import com.metaaps.webapps.libraries.client.widget.util.WidgetUtil;
import com.metaaps.webapps.libraries.client.widget.util.WidgetUtil.NotWidgetTypeException;

import java.util.List;

public class SelectAoIsImportDialog extends Composite {

	private static SelectObservationImportDialogUiBinder uiBinder = GWT
			.create(SelectObservationImportDialogUiBinder.class);
	private static SelectAoIsImportDialog instance;

	interface SelectObservationImportDialogUiBinder extends
			UiBinder<Widget, SelectAoIsImportDialog> {
	}

    static public interface Style extends CssResource {

        String aoiWidget();
    }

    @UiField Style style;
	
	@UiField
    BasePopup popup;
	@UiField Label message;
	@UiField FlowPanel listAoIs;

	private EventBus eventBus;
	
	public SelectAoIsImportDialog(EventBus eventBus) {
		
		this.eventBus = eventBus;

		initWidget(uiBinder.createAndBindUi(this));
		
		popup.setAutoHideEnabled(false);
		popup.setGlassEnabled(false);
		popup.setModal(true);
		
	}

	static public SelectAoIsImportDialog getInstance(EventBus eventBus) {
		if(instance == null) {
			instance = new SelectAoIsImportDialog(eventBus);
		}
		instance.setEventBus(eventBus);
		return instance;
	}
	
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}

    class AoIWidget extends HTMLPanel {

        private RadioButton radioButton;

        AOI aoi;

        public AoIWidget(String name) {
            super("");
            getElement().getStyle().setPadding(5, com.google.gwt.dom.client.Style.Unit.PX);
            radioButton = new RadioButton("aoi", name);
            radioButton.addStyleName(style.aoiWidget());
            add(radioButton);
        }

        public boolean isSelected() {
            return radioButton.getValue();
        }
    }
	
	public void setAOIs(List<AOI> aois) {
		message.setText(aois.size() + " AOIs were found in the file, select one");
		listAoIs.clear();
		for(AOI aoi : aois) {
            AoIWidget aoiSelect = new AoIWidget(aoi.getName());
            aoiSelect.aoi = aoi;
			listAoIs.add(aoiSelect);
		}
	}
	
	@UiHandler("okButton")
	void onOKButton(ClickEvent event) {
		popup.hide();
		List<AOI> aois = WidgetUtil.getValues(listAoIs, new WidgetUtil.GetValue<AOI>() {

            @Override
            public AOI getValue(Widget widget) throws NotWidgetTypeException {
                if (widget instanceof AoIWidget && ((AoIWidget) widget).isSelected()) {
                    return ((AoIWidget) widget).aoi;
                } else {
                    throw new NotWidgetTypeException();
                }
            }
        });
		if(aois.size() > 0) {
            eventBus.fireEvent(new AddAoIImported(aois.get(0)));
		}
	}

	@UiHandler("cancelButton")
	void cancel(ClickEvent event) {
		popup.hide();
	}
	
	public void showAt(List<AOI> aois, Widget parentWidget, TYPE type) {
		setAOIs(aois);
		popup.showAt(parentWidget, type);
	}
	
}
