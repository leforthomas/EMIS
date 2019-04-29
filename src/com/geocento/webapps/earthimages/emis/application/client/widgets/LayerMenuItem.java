package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.CoverageDTO;
import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.UserLayerDTO;
import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.application.client.event.TimeChangedEvent;
import com.geocento.webapps.earthimages.emis.application.client.utils.WCSCapabilities;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.Tooltip;
import com.metaaps.webapps.libraries.client.widget.style.StyleResources;
import com.metaaps.webapps.libraries.client.widget.ValueSpinner;
import com.metaaps.webapps.libraries.client.widget.util.*;

import java.util.Date;

public class LayerMenuItem extends Composite {

	public static enum TYPE {WMS, TMS};
	
	public static interface Presenter {
		void layerChanged(UserLayerDTO layer);
		void zoomToLayer(UserLayerDTO layer);
		void deleteLayer(UserLayerDTO layer);
		void onLayerVisibilityChanged(UserLayerDTO layer);
		void moveLayerZIndex(UserLayerDTO layer, String value);
        void startWCSSelection(UserLayerDTO userLayerDTO);
    }
	
	static protected StyleResources styleResources = StyleResources.INSTANCE;

    private static LayerMenuItemUiBinder uiBinder = GWT
			.create(LayerMenuItemUiBinder.class);

	interface LayerMenuItemUiBinder extends UiBinder<Widget, LayerMenuItem> {
	}

	public interface Style extends CssResource {
		String menuItemSelected();

		String draggedOver();
	}

	@UiField Style style;

	@UiField Image icon;
	@UiField Label label;
	@UiField Image zoom;
	@UiField Image edit;
	@UiField HTMLPanel editPanel;
	@UiField Grid editGrid;
	@UiField(provided=true)
    ValueSpinner opacity;
    @UiField
    Image display;
    @UiField
    Image delete;

    ListBox styles;
	
	private UserLayerDTO layer;

	private Presenter presenter;

    public LayerMenuItem(UserLayerDTO layer) {

        opacity = new ValueSpinner(0, 0, 100);
		
		initWidget(uiBinder.createAndBindUi(this));
		
		setLayer(layer);

		Utils.setDraggable(icon, true, "Drag me in the list to change my z-index", Util.TYPE.right);
		icon.addDragStartHandler(event -> {
            event.getDataTransfer().setDragImage(LayerMenuItem.this.getElement(), 0, 0);
            event.getDataTransfer().setData("text", LayerMenuItem.this.layer.getLayerDTO().getLayerName() + "");
        });
		
		addDomHandler(event -> {
            event.preventDefault();
            String value = event.getData("text");
            if(value != null) {
                addStyleName(style.draggedOver());
            }
        }, DragOverEvent.getType());
		
		addDomHandler(event -> {
            event.preventDefault();
            removeStyleName(style.draggedOver());
        }, DragEndEvent.getType());
		
		addDomHandler(event -> removeStyleName(style.draggedOver()), DragLeaveEvent.getType());
		
		addDomHandler(event -> {
            event.preventDefault();
            String value = event.getData("text");
            if(value != null) {
                removeStyleName(style.draggedOver());
                if(LayerMenuItem.this.presenter != null) {
                    LayerMenuItem.this.presenter.moveLayerZIndex(LayerMenuItem.this.layer, value);
                }
            }
        }, DropEvent.getType());

	}

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    private void setWidgetSelected(boolean isSelected) {
        if(isSelected) {
            label.addStyleName(style.menuItemSelected());
        } else {
            label.removeStyleName(style.menuItemSelected());
        }
        display.getElement().getStyle().setOpacity(isSelected ? 1.0 : 0.3);
        // collapse the editing
        setEditing(false);
        // update all buttons
        updateButtonsDisplay();
    }

    public void setSelected(boolean isSelected) {
        this.layer.setVisible(isSelected);
        setWidgetSelected(isSelected);
	}
	
	private void setEditing(boolean editing) {
		editPanel.setVisible(editing);
	}

    @UiHandler("display")
	void select(ClickEvent clickEvent) {
		setSelected(!isSelected());
		if(presenter != null) {
			presenter.onLayerVisibilityChanged(layer);
		}
	}

	@UiHandler("edit")
	void edit(ClickEvent clickEvent) {
		setEditing(!editPanel.isVisible());
	}
	
	@UiHandler("zoom")
	void zoom(ClickEvent clickEvent) {
		if(presenter != null) {
			presenter.zoomToLayer(layer);
		}
	}

	@UiHandler("delete")
	void delete(ClickEvent clickEvent) {
		if(presenter != null) {
			presenter.deleteLayer(layer);
		}
	}
	
	public boolean isSelected() {
		return layer.isVisible();
	}

	private void setLayer(UserLayerDTO userLayerDTO) {
		this.layer = userLayerDTO;
        LayerDTO layerDTO = userLayerDTO.getLayerDTO();
        icon.setResource(layerDTO.isTimeEnabled() ? com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.timeLayer() :
                com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.layer());
		String name = layerDTO.getName();
		label.setText(StringUtils.isEmpty(name) ? "No name" : name);
		setWidgetSelected(userLayerDTO.isVisible());
        Tooltip.getLeftLabel().registerTooltip(this, layerDTO.getDescription());
		opacity.setValue(userLayerDTO.getOpacity());
		opacity.setChangeHandler(value -> {
            LayerMenuItem.this.layer.setOpacity(value.intValue());
            if(presenter != null) {
                presenter.layerChanged(LayerMenuItem.this.layer);
            }
        });
		if(layerDTO.isTimeEnabled()) {
            ListBox listBox = new ListBox();
            for(Date date : layerDTO.getDates()) {
                listBox.addItem(DateUtil.displayUTCDate(date), date.getTime() + "");
            }
            WidgetUtil.addGridWidgetProperty(editGrid, "Time", listBox);
            if(userLayerDTO.getSelectedTime() != null) {
                WidgetUtil.selectListBoxItem(listBox, userLayerDTO.getSelectedTime().getTime() + "");
            }
            listBox.addChangeHandler(event -> Application.clientFactory.getEventBus().fireEvent(new TimeChangedEvent(new Date(Long.parseLong(listBox.getSelectedValue())))));
        }
        CoverageDTO coverageDTO = layerDTO.getCoverageDTO();
        if(coverageDTO != null) {
            ListBox listBox = new ListBox();
            for(String choice : new String[] {"Choose", "Download selection", "Download full"}) {
                listBox.addItem(choice);
            }
            listBox.addChangeHandler(event -> {
                switch (listBox.getSelectedIndex()) {
                    case 1: {
                        presenter.startWCSSelection(userLayerDTO);
                    } break;
                    case 2: {
                        Window.open(WCSCapabilities.getDownloadCoverageUrl(coverageDTO, null, userLayerDTO.getSelectedTime()), "_blank", null);
                    } break;
                }
            });
            WidgetUtil.addGridWidgetProperty(editGrid, "Download", listBox);
        }
		updateButtonsDisplay();
	}

	private void updateButtonsDisplay() {
		// check we can zoom
		edit.setVisible(layer.isVisible());
		zoom.setVisible(layer.isVisible() && layer.getLayerDTO().getBounds() != null);
	}

	public boolean isLayer(UserLayerDTO layer) {
		return this.layer == layer;
	}

}
