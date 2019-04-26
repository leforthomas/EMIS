package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.UserLayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOIRectangle;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.utils.UserLayersHelper;
import com.geocento.webapps.earthimages.emis.application.client.utils.WCSCapabilities;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.map.LocationService;
import com.metaaps.webapps.libraries.client.mapwidgets.EOLatLngCenterPopup;
import com.metaaps.webapps.libraries.client.widget.*;
import com.metaaps.webapps.libraries.client.widget.PopUpMenu.IPopupMenuEventListener;
import com.metaaps.webapps.libraries.client.widget.util.EditCompletedListener;
import com.metaaps.webapps.libraries.client.widget.util.Util;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;

import java.util.List;

public class MapTools extends Composite {

    static public interface Presenter {

		void onBaseMapChanged(String mapId);

		void onLayerChanged(String layer);

		void onMapLibraryChanged(String mapLibrary);

        void mapExtentChanged();
    }

	static protected StyleResources styleResources = StyleResources.INSTANCE;

    static private ArrowedPopUpMenu popupMenu;

    private static MapToolsUiBinder uiBinder = GWT
			.create(MapToolsUiBinder.class);

	interface MapToolsUiBinder extends UiBinder<Widget, MapTools> {
	}
	
	interface Style extends CssResource {
		String delimiter();

		String toolbarMenu();

		String horizontalPanelBottom();

        String horizontalResultPanel();

        String withDelimiter();

        String addLayer();
    }

	@UiField Style style;
	
	@UiField
	protected HorizontalPanel panel;
	@UiField
	ToggledIconAnchor displayCoordinates;
	@UiField ToggledIconAnchor showgrid;
	@UiField IconAnchor setCenter;
	@UiField IconAnchor mapLibrary;
	@UiField IconAnchor baseMap;
    @UiField
    IconAnchor worldZoom;
    @UiField
    IconAnchor zoomBack;
    @UiField
    IconAnchor wmsLayers;
    @UiField
    ToggledIconAnchor featureInfo;

    protected MapPanel mapPanel;

    private EOLatLngCenterPopup popupPanel;

    private boolean loading;

    private LocationService locationService;

    private Presenter presenter;

	public MapTools() {
		
		initWidget(uiBinder.createAndBindUi(this));

        worldZoom.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                mapPanel.setEOBounds(new EOBounds(new EOLatLng(1.0, 180.0), new EOLatLng(-1.0, -180.0)));
                if (presenter != null) {
                    presenter.mapExtentChanged();
                }
            }
        });
        zoomBack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(!mapPanel.hasPreviousMapExtents()) {
                    Tooltip.getTooltip().display("No map extent to go back to", zoomBack, TYPE.below);
                } else {
                    mapPanel.toPreviousMapExtent();
                    if (presenter != null) {
                        presenter.mapExtentChanged();
                    }
                }
            }
        });
		displayCoordinates.setToggleHandler(toggled -> displayCoordinates(toggled));
		showgrid.setToggleHandler(toggled -> showGrid(toggled));
        mapLibrary.addClickHandler(clickEvent -> displayMapLibraries());
		baseMap.addClickHandler(clickEvent -> displayBaseMap());

        featureInfo.setToggleHandler(toggled -> mapPanel.enableFeatureInfo(toggled, featureInfo));

        featureInfo.setVisible(false);

        wmsLayers.addClickHandler(clickEvent -> showLayers());

        if(popupMenu == null) {
            popupMenu = new ArrowedPopUpMenu();
        }

    }

    public void displayLayersMenu(boolean display) {
        wmsLayers.setVisible(display);
    }

    private void initLayers() {
        mapPanel.clearLayers();
        this.loading = true;
        UserLayersHelper.getUserLayers(new AsyncCallback<List<UserLayerDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                loading = false;
            }

            @Override
            public void onSuccess(List<UserLayerDTO> result) {
                loading = false;
                for (UserLayerDTO layerDTO : result) {
                    handleLayerVisibility(layerDTO);
                }
            }
        });
    }

    private void handleLayerVisibility(UserLayerDTO layer) {
        boolean displayLayers = wmsLayers.isVisible();
        if(displayLayers && layer.isVisible()) {
            mapPanel.addLayer(layer);
        } else {
            mapPanel.removeLayer(layer);
        }
    }

    private void showLayers() {
        popupMenu.hide();
        popupMenu.clearItems();

        List<UserLayerDTO> layersList = UserLayersHelper.getUserLayers();
        if(loading) {
            LoadingIcon loadingIcon = new LoadingIcon("Loading layers...");
            loadingIcon.getElement().getStyle().setWhiteSpace(com.google.gwt.dom.client.Style.WhiteSpace.NOWRAP);
            popupMenu.addWidget(loadingIcon);
        } else if (layersList == null) {
            ErrorIcon errorIcon = new ErrorIcon("Error loading layers...");
            errorIcon.getElement().getStyle().setWhiteSpace(com.google.gwt.dom.client.Style.WhiteSpace.NOWRAP);
            popupMenu.addWidget(errorIcon);
        } else {
            if (layersList.size() == 0) {
                popupMenu.addWidget(new Label("No layers..."));
            } else {
                for (UserLayerDTO userLayerDTO : layersList) {
                    LayerMenuItem layerMenuItem = new LayerMenuItem(userLayerDTO);
                    layerMenuItem.setPresenter(new LayerMenuItem.Presenter() {

                        @Override
                        public void layerChanged(UserLayerDTO layer) {
                            mapPanel.updateLayer(layer);
                        }

                        @Override
                        public void zoomToLayer(UserLayerDTO layer) {
                            mapPanel.setEOBounds(layer.getLayerDTO().getBounds());
                        }

                        @Override
                        public void deleteLayer(UserLayerDTO layer) {
                            removeLayer(layer);
                        }

                        @Override
                        public void onLayerVisibilityChanged(UserLayerDTO layer) {
                            handleLayerVisibility(layer);
                            featureInfo.setVisible(UserLayersHelper.checkInfoVisibility());
                        }

                        @Override
                        public void moveLayerZIndex(UserLayerDTO layer, String layerName) {
                            if (layer == null || layerName == null) {
                                return;
                            }
                            UserLayerDTO layerMoved = null;
                            // find layer with the corresponding baseUrl
                            for (UserLayerDTO baseUrlLayer : layersList) {
                                if (baseUrlLayer != layer && (baseUrlLayer.getLayerDTO().getLayerName() + "").contentEquals(layerName)) {
                                    layerMoved = (UserLayerDTO) baseUrlLayer;
                                }
                            }
                            if (layerMoved != null) {
                                layersList.remove(layerMoved);
                                layersList.add(layersList.indexOf(layer), layerMoved);
                                updateZIndexes();
                                showLayers();
                            }
                        }

                        @Override
                        public void startWCSSelection(UserLayerDTO userLayerDTO) {
                            AOIRectangle aoiRectangle = new AOIRectangle();
                            aoiRectangle.setStrokeColor("33ff33");
                            aoiRectangle.setStrokeThickness(1);
                            aoiRectangle.setFillOpacity(0.1);
                            aoiRectangle.setFillColor("33ff33");
                            mapPanel.drawNewFeature(aoiRectangle, new AsyncCallback<AOI>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Issue querying WCS service for layer, error is " + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(AOI result) {
                                    String requestUrl = WCSCapabilities.getDownloadCoverageUrl(userLayerDTO.getLayerDTO().getCoverageDTO(), aoiRectangle.getBounds(), userLayerDTO.getSelectedTime());
                                    Window.open(requestUrl, "_blank", null);
                                }
                            });
                        }

                    });
                    popupMenu.addWidget(layerMenuItem);
                }
            }
            // add a menu for adding layers
            popupMenu.addMenuDivider();
            Label addLayers = new Label("Add new layer");
            addLayers.addStyleName(style.addLayer());
            addLayers.addClickHandler(event -> {
                popupMenu.hide();
                displaySelectLayers();
            });
            popupMenu.addWidget(addLayers);
        }
        popupMenu.showAt(wmsLayers, Util.TYPE.below);
    }

    private void displaySelectLayers() {
        popupMenu.clearItems();
        LayersTreeWidget layersTreeWidget = new LayersTreeWidget();
        layersTreeWidget.getElement().getStyle().setWidth(250, com.google.gwt.dom.client.Style.Unit.PX);
        layersTreeWidget.getElement().getStyle().setProperty("maxHeight", (Window.getClientHeight() - this.getAbsoluteTop() - 50 - 60) + "px");
        layersTreeWidget.addStyleName(StyleResources.INSTANCE.style().scrollVertical());
        layersTreeWidget.addStyleName(com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.style().slimScrollbar());
        popupMenu.addWidget(layersTreeWidget);
        // add a menu for adding layers
        popupMenu.addMenuDivider();
        Label addLayers = new Label("Add selected layers");
        addLayers.addStyleName(style.addLayer());
        addLayers.addClickHandler(event -> {
            // TODO - add selected layers to the user layers
            List<LayerDTO> selectedLayers = layersTreeWidget.getSelectedLayers();
            if(selectedLayers.size() == 0) {
                Window.alert("Select some layers first");
                return;
            }
            popupMenu.hide();
            popupMenu.clearItems();
            LoadingIcon loadingIcon = new LoadingIcon("Adding layers...");
            loadingIcon.getElement().getStyle().setWhiteSpace(com.google.gwt.dom.client.Style.WhiteSpace.NOWRAP);
            popupMenu.addWidget(loadingIcon);
            popupMenu.showAt(wmsLayers, Util.TYPE.below);
            CustomerService.App.getInstance().addUserLayers(selectedLayers, new AsyncCallback<List<UserLayerDTO>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Error adding layers, please try again");
                    displaySelectLayers();
                }

                @Override
                public void onSuccess(List<UserLayerDTO> userLayers) {
                    popupMenu.hide();
                    for(UserLayerDTO userLayerDTO : userLayers) {
                        userLayerDTO.setVisible(true);
                        userLayerDTO.setOpacity(100);
                    }
                    UserLayersHelper.addLayers(userLayers);
                    showLayers();
                    for(UserLayerDTO userLayerDTO : userLayers) {
                        handleLayerVisibility(userLayerDTO);
                    }
                    featureInfo.setVisible(UserLayersHelper.checkInfoVisibility());
                }
            });

        });
        popupMenu.addWidget(addLayers);
        popupMenu.showAt(wmsLayers, Util.TYPE.below);
    }

    private void updateZIndexes() {

        int zIndex = 0;
        List<UserLayerDTO> layersList = UserLayersHelper.getUserLayers();
        if(layersList == null || layersList.size() == 0) {
            return;
        }

        for(UserLayerDTO layer : layersList) {
            if(layer.isVisible()) {
                layer.setzIndex(zIndex++);
            }
        }
        mapPanel.updateLayers();
    }

    private void removeLayer(UserLayerDTO layer) {
        CustomerService.App.getInstance().deleteUserLayer(layer.getId(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                popupMenu.clearItems();
                popupMenu.addMenuItem(new ErrorIcon("Error removing layer..."));
            }

            @Override
            public void onSuccess(Void result) {
                popupMenu.hide();
                UserLayersHelper.removeLayer(layer);
                layer.setVisible(false);
                mapPanel.removeLayer(layer);
                showLayers();
                featureInfo.setVisible(UserLayersHelper.checkInfoVisibility());
            }
        });
    }

    public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
        mapLibrary.setVisible(mapPanel.getMapLibraries().size() > 1);
        // update the tools display
		displayCoordinates(false);
        //We need to have the map created to add/remove layers
        initLayers();
	}

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
        setCenterEnabled(true);
    }

    private void setCenterEnabled(boolean enabled) {
        setCenter.setVisible(enabled);
    }

    public void setChangeLibraryEnabled(boolean enabled) {
        mapLibrary.setVisible(enabled);
    }

    public void setDisplayCoordinatesEnabled(boolean enabled) {
        displayCoordinates.setVisible(enabled);
    }

    public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

    public void addWithDelimiter(Widget widget) {
        widget.addStyleName(style.withDelimiter());
        add(widget);
    }

    public void addDelimiter() {
        HTMLPanel delimiter = new HTMLPanel("");
        delimiter.addStyleName(style.delimiter());
        panel.add(delimiter);
    }

    public void displayCoordinates(boolean display) {
		displayCoordinates.setToggled(display);
		mapPanel.displayCoordinates(display);
        mapPanel.getMap().triggerResize();
	}
	
	public void showGrid(boolean display) {
		showgrid.setToggled(display);
		mapPanel.displayCoordinatesGrid(display);
	}
	
	@UiHandler("setCenter")
	public void setCenter(ClickEvent event) {
		if(popupPanel == null) {
			popupPanel = new EOLatLngCenterPopup(locationService);
			popupPanel.setZoomToExtent(true);
			popupPanel.addCompleted(new EditCompletedListener() {

				@Override
				public void onEditCancelled() {
				}

				@Override
				public void onEditCompleted(Object position) {
					//clientFactory.getEventBus().fireEvent(new CenterMapEvent((EOLatLng) position, popupPanel.getExtent()));
					if(popupPanel.getExtent() == null) {
						mapPanel.setCenter((EOLatLng) position);
					} else {
						mapPanel.setEOBounds(popupPanel.getExtent());
					}
				}
				
			});
		}
		popupPanel.showAt(setCenter, TYPE.below);
	}

	private void displayMapLibraries() {
		popupMenu.clearItems();
		String currentMapLibrary = mapPanel.getMapLibrary();
		for(final String mapLibrary : mapPanel.getMapLibraries()) {
			boolean selected = mapLibrary.contentEquals(currentMapLibrary);
			SelectionWidget mapLabel = new SelectionWidget();
			mapLabel.setText(mapLibrary);
			mapLabel.setSelected(selected);
			popupMenu.addMenuItem(new IPopupMenuEventListener() {

				@Override
				public void execute() {
					popupMenu.hide();
					if(presenter != null) {
						presenter.onMapLibraryChanged(mapLibrary);
					}
				}
			}, mapLabel);
		}
		popupMenu.showAt(mapLibrary, TYPE.below);
	}

	private void displayBaseMap() {
		popupMenu.clearItems();
		String currentMapId = mapPanel.getMapId();
		for(final String mapId : mapPanel.getMapIds()) {
			boolean selected = mapId.contentEquals(currentMapId);
			SelectionWidget mapLabel = new SelectionWidget();
			mapLabel.setText(mapId);
			mapLabel.setSelected(selected);
			popupMenu.addMenuItem(new IPopupMenuEventListener() {

				@Override
				public void execute() {
					mapPanel.setMapId(mapId);
					popupMenu.hide();
					if(presenter != null) {
						presenter.onBaseMapChanged(mapId);
					}
				}
			}, mapLabel);
		}
		popupMenu.showAt(baseMap, TYPE.below);
	}

	public IconAnchor addButton(ImageResource resource, String tooltipText) {
		IconAnchor iconAnchor = new IconAnchor();
		iconAnchor.setResource(resource);
		iconAnchor.setTooltip(tooltipText, TYPE.below);
		iconAnchor.addStyleName(style.toolbarMenu());
		panel.add(iconAnchor);

		return iconAnchor;
	}

	public IconAnchor addButton(ImageResource resource, String tooltipText, ClickHandler clickHandler) {
		IconAnchor iconAnchor = addButton(resource, tooltipText);
		iconAnchor.addClickHandler(clickHandler);
		return iconAnchor;
	}

	public ToggledIconAnchor addToggleButton(ImageResource resource, String tooltipText, ToggleHandler toggleHandler) {
		ToggledIconAnchor toggleIconAnchor = new ToggledIconAnchor();
		toggleIconAnchor.setResource(resource);
		toggleIconAnchor.setTooltip(tooltipText, TYPE.below);
		toggleIconAnchor.addStyleName(style.toolbarMenu());
		toggleIconAnchor.setToggleHandler(toggleHandler);

		panel.add(toggleIconAnchor);

		return toggleIconAnchor;
	}

	public void add(Widget widget) {
		panel.add(widget);
	}

}
