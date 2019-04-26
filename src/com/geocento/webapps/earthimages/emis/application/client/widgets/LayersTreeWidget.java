package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.share.CoverageDTO;
import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.LayerResource;
import com.geocento.webapps.earthimages.emis.common.share.WMSServerDTO;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.utils.WCSCapabilities;
import com.geocento.webapps.earthimages.emis.application.client.utils.WMSCapabilities;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.ErrorIcon;
import com.metaaps.webapps.libraries.client.widget.IconLabel;
import com.metaaps.webapps.libraries.client.widget.LoadingIcon;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class LayersTreeWidget extends BaseTreeWidget implements OpenHandler<TreeItem> {

    public LayersTreeWidget() {
        tree.addOpenHandler(this);
        loadLayers();
    }

    private void loadLayers() {
        tree.clear();
        tree.addItem(new TreeItem(new LoadingIcon("Loading layers...")));
        CustomerService.App.getInstance().loadAvailableLayers(null, new AsyncCallback<List<LayerResource>>() {

            @Override
            public void onFailure(Throwable caught) {
                tree.clear();
                tree.addItem(new TreeItem(new ErrorIcon("Failed to load layers...")));
            }

            @Override
            public void onSuccess(List<LayerResource> result) {
                tree.clear();
                addLayerResources(null, result);
            }
        });
    }

    private void addLayerResources(TreeItem baseTreeItem, List<? extends LayerResource> layerResources) {
        if(layerResources == null || layerResources.size() == 0) {
            TreeItem treeItem = new TreeItem(new Label("No data..."));
            if(baseTreeItem == null) {
                tree.addItem(treeItem);
            } else {
                baseTreeItem.addItem(treeItem);
            }
            return;
        }
        for(LayerResource layerResource : layerResources) {
            TreeItem treeItem = null;
            if (layerResource instanceof WMSServerDTO) {
                WMSServerDTO wmsServerDTO = (WMSServerDTO) layerResource;
                treeItem = new TreeItem(new WMSServerWidget(wmsServerDTO));
                if (wmsServerDTO.getLayers() != null) {
                    addLayerResources(treeItem, wmsServerDTO.getLayers());
                } else {
                    treeItem.addItem(new TreeItem(new LoadingIcon("loading...")));
                }
            } else if(layerResource instanceof LayerDTO) {
                treeItem = new TreeItem(new LayerWidget((LayerDTO) layerResource));
            } else {
                treeItem = new TreeItem(new ErrorIcon("Unknown resource type"));
            }
            if(baseTreeItem == null) {
                tree.addItem(treeItem);
            } else {
                baseTreeItem.addItem(treeItem);
            }
        }
    }

    static class IconWidget<T> extends IconLabel {

        T resource;

        public IconWidget(ImageResource image, String label) {
            super(image, label);
        }

        public void setResource(T resource) {
            this.resource = resource;
        }

        public T getResource() {
            return resource;
        }
    }

    static class WMSServerWidget extends IconWidget<WMSServerDTO> {
        public WMSServerWidget(WMSServerDTO resource) {
            super(null, resource.getName());
            getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
            setResource(resource);
            setTooltip(resource.getDescription());
        }
    }

    static class LayerWidget extends SelectIconWidget<LayerDTO> {

        public LayerWidget(LayerDTO resource) {
            super(resource);
            setImageResource(resource.isTimeEnabled() ? com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.timeLayer() :
                    com.geocento.webapps.earthimages.emis.common.client.style.StyleResources.INSTANCE.layer());
            // check if it has wcs enabled
            if(resource.getCoverageDTO() != null) {
                Image wcsIcon = new Image(StyleResources.INSTANCE.download());
                wcsIcon.setWidth("8px");
                wcsIcon.setHeight("8px");
                wcsIcon.getElement().getStyle().setFloat(Style.Float.RIGHT);
                wcsIcon.setTitle("Layer has WCS service enabled");
                DOM.appendChild(getElement(), wcsIcon.getElement());
            }
            setLabel(resource.getName());
            setTitle(resource.getDescription());
            setSelectionState(Utils.SELECTION_STATE.UNSELECTED);
        }
    }

    @Override
    public void onOpen(OpenEvent<TreeItem> event) {
        final TreeItem treeItem = event.getTarget();
        if(treeItem.getWidget() instanceof WMSServerWidget) {
            // load on folder widget expanded
            final WMSServerDTO resource = ((WMSServerWidget) treeItem.getWidget()).getResource();
            // check if data has been loaded
            if(resource.getLayers() != null) {
                return;
            }
            if(resource.getBaseUrl() != null) {
                WMSCapabilities wmsCapabilities = new WMSCapabilities();
                wmsCapabilities.loadWMSCapabilities(resource.getBaseUrl(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        treeItem.removeItems();
                        treeItem.addItem(new TreeItem(new ErrorIcon("Failed to load layers...")));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        ArrayList<LayerDTO> layers = wmsCapabilities.getLayersList();
                        if(!StringUtils.isEmpty(resource.getWCSUrl())) {
                            // load WCS resources and try to match up with WMS layers
                            WCSCapabilities wcsCapabilities = new WCSCapabilities();
                            wcsCapabilities.loadWCSCapabilities(resource.getWCSUrl(), new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    addResources(layers);
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    for(LayerDTO layerDTO : layers) {
                                        CoverageDTO coverageDTO = ListUtil.findValue(wcsCapabilities.getCoveragesList(), value -> value.getCoverageId().contains(layerDTO.getLayerName()));
                                        if (coverageDTO == null) {
                                            com.geocento.webapps.earthimages.emis.common.client.utils.Utils.printLog("Could not find matching coverage for " + layerDTO.getLayerName());
                                        } else {
                                            layerDTO.setCoverageDTO(coverageDTO);
                                        }
                                    }
                                    addResources(layers);
                                }
                            });
                        } else {
                            addResources(layers);
                        }
                    }

                    private void addResources(ArrayList<LayerDTO> layers) {
                        treeItem.removeItems();
                        addLayerResources(treeItem, layers);
                    }
                });
            } else {
                CustomerService.App.getInstance().loadAvailableLayers(resource.getId(), new AsyncCallback<List<LayerResource>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        treeItem.removeItems();
                        treeItem.addItem(new TreeItem(new ErrorIcon("Failed to load layers...")));
                    }

                    @Override
                    public void onSuccess(List<LayerResource> result) {
                        treeItem.removeItems();
                        addLayerResources(treeItem, result);
                    }
                });
            }
        }
    }

    public List<LayerDTO> getSelectedLayers() {
        List<LayerDTO> selectedLayers = new ArrayList<LayerDTO>();
        // scan the tree item for selected layers
        for(int index = 0; index < tree.getItemCount(); index++) {
            TreeItem treeItem = tree.getItem(index);
            Widget widget = treeItem.getWidget();
            if(widget instanceof LayerWidget && ((LayerWidget) widget).getSelectionState() == Utils.SELECTION_STATE.SELECTED) {
                selectedLayers.add(((LayerWidget) widget).resource);
            }
            if(treeItem.getChildCount() > 0) {
                selectedLayers.addAll(getSelectedLayers(treeItem));
            }
        }
        return selectedLayers;
    }

    public List<LayerDTO> getSelectedLayers(TreeItem baseTreeItem) {
        List<LayerDTO> selectedLayers = new ArrayList<LayerDTO>();
        // scan the tree item for selected layers
        for(int index = 0; index < baseTreeItem.getChildCount(); index++) {
            TreeItem treeItem = baseTreeItem.getChild(index);
            Widget widget = treeItem.getWidget();
            if(widget instanceof LayerWidget && ((LayerWidget) widget).getSelectionState() == Utils.SELECTION_STATE.SELECTED) {
                selectedLayers.add(((LayerWidget) widget).resource);
            }
            if(treeItem.getChildCount() > 0) {
                selectedLayers.addAll(getSelectedLayers(treeItem));
            }
        }
        return selectedLayers;
    }
}
