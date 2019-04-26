package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.CoverageDTO;
import com.geocento.webapps.earthimages.emis.common.share.LayerDTO;
import com.geocento.webapps.earthimages.emis.common.share.UserLayerDTO;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomas on 28/01/2015.
 */
public class UserLayersHelper {

    static List<UserLayerDTO> userLayers = null;

    private static Date currentTime;

    public static void getUserLayers(AsyncCallback<List<UserLayerDTO>> asyncCallback) {
        if (userLayers == null) {
            CustomerService.App.getInstance().loadUserLayers(new AsyncCallback<List<UserLayerDTO>>() {

                public int completedCalls = 0;

                @Override
                public void onFailure(Throwable caught) {
                    asyncCallback.onFailure(caught);
                }

                @Override
                public void onSuccess(List<UserLayerDTO> userLayers) {
                    UserLayersHelper.userLayers = userLayers;
                    if(userLayers.size() == 0) {
                        checkWMSCompleted();
                    }
                    // group layers by server base address
                    HashMap<String, List<UserLayerDTO>> layerServers = ListUtil.group(userLayers, new ListUtil.GetValue<String, UserLayerDTO>() {
                        @Override
                        public String getLabel(UserLayerDTO value) {
                            return value.getLayerDTO().getBaseUrl();
                        }

                        @Override
                        public <UserLayerDTO> List<UserLayerDTO> createList() {
                            return new ArrayList<UserLayerDTO>();
                        }
                    });
                    completedCalls = layerServers.size();
                    // now fetch the servers
                    for(String baseUrl : layerServers.keySet()) {
                        Utils.printLog("Calling wms server " + baseUrl);
                        WMSCapabilities wmsCapabilities = new WMSCapabilities();
                        wmsCapabilities.loadWMSCapabilities(baseUrl, new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                // TODO - flag layer as not valid?
                                Utils.printLog("Failed call for server " + baseUrl);
                                completedCalls--;
                                checkWMSCompleted();
                            }

                            @Override
                            public void onSuccess(Void result) {
                                List<UserLayerDTO> serverLayers = layerServers.get(baseUrl);
                                for(UserLayerDTO userLayerDTO : serverLayers) {
                                    LayerDTO layerDTO = ListUtil.findValue(wmsCapabilities.getLayersList(), value -> value.getLayerName().contentEquals(userLayerDTO.getLayerDTO().getLayerName()));
                                    if (layerDTO == null) {
                                        // TODO - remove layer or flag as issue
                                    } else {
                                        // keep the layer title the same as layer groups are not configured correctly for the publisher
                                        layerDTO.setName(userLayerDTO.getLayerDTO().getName());
                                        layerDTO.setDescription(userLayerDTO.getLayerDTO().getDescription());
                                        layerDTO.setCoverageDTO(userLayerDTO.getLayerDTO().getCoverageDTO());
                                        userLayerDTO.setLayerDTO(layerDTO);
                                        userLayerDTO.setOpacity(100);
                                    }
                                }
                                Utils.printLog("Completed call for server " + baseUrl);
                                completedCalls--;
                                checkWMSCompleted();
                            }
                        });
                    }
                }

                private void checkWMSCompleted() {
                    Utils.printLog("Checking completed, value is " + completedCalls);
                    if(completedCalls == 0) {
                        Utils.printLog("Completed calls, now updating layers");
                        UserLayersHelper.setUserLayers(userLayers);
                        // update time
                        updateLayersTime();
                        //asyncCallback.onSuccess(userLayers);
                        Utils.printLog("Now checking for WCS services");
                        // group layers by server base address
                        // TODO - check somehow whether WCS is available
                        HashMap<String, List<LayerDTO>> layerWCSServers = new HashMap<String, List<LayerDTO>>();
                        for(UserLayerDTO userLayerDTO : userLayers) {
                            CoverageDTO coverageDTO = userLayerDTO.getLayerDTO().getCoverageDTO();
                            if(coverageDTO != null) {
                                List<LayerDTO> wcsLayers = layerWCSServers.get(coverageDTO.getBaseUrl());
                                if(wcsLayers == null) {
                                    wcsLayers = new ArrayList<LayerDTO>();
                                    layerWCSServers.put(coverageDTO.getBaseUrl(), wcsLayers);
                                }
                                wcsLayers.add(userLayerDTO.getLayerDTO());
                            }
                        }
                        completedCalls = layerWCSServers.size();
                        if(completedCalls == 0) {
                            checkWCSCompleted();
                        }
                        // now fetch the servers
                        for(String baseUrl : layerWCSServers.keySet()) {
                            Utils.printLog("Calling WCS server " + baseUrl);
                            WCSCapabilities wcsCapabilities = new WCSCapabilities();
                            wcsCapabilities.loadWCSCapabilities(baseUrl, new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    // TODO - flag layer as not valid?
                                    Utils.printLog("Failed to call WCS service for base URL " + baseUrl);
                                    completedCalls--;
                                    checkWCSCompleted();
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    List<LayerDTO> serverLayers = layerWCSServers.get(baseUrl);
                                    for(LayerDTO layerDTO : serverLayers) {
                                        CoverageDTO coverageDTO = ListUtil.findValue(wcsCapabilities.getCoveragesList(), value -> value.getCoverageId().contentEquals(layerDTO.getCoverageDTO().getCoverageId()));
                                        if (coverageDTO == null) {
                                            Utils.printLog("Could not find matching coverage for " + layerDTO.getCoverageDTO().getCoverageId());
                                            layerDTO.setCoverageDTO(null);
                                        } else {
                                            // update missing parameters
                                            layerDTO.getCoverageDTO().setVersion(coverageDTO.getVersion());
                                        }
                                    }
                                    completedCalls--;
                                    checkWCSCompleted();
                                }
                            });
                        }
                    }
                }

                private void checkWCSCompleted() {
                    Utils.printLog("Checking WCS completed, value is " + completedCalls);
                    if(completedCalls == 0) {
                        Utils.printLog("Completed WCS now calling callback");
                        // now look for WCS services
                        asyncCallback.onSuccess(userLayers);
                    }
                }
            });
        } else {
            asyncCallback.onSuccess(userLayers);
        }
    }

    public static List<UserLayerDTO> getUserLayers() {
        return userLayers;
    }

    public static void setUserLayers(List<UserLayerDTO> layers) {
        userLayers = layers;
    }

    public static boolean layerExists(UserLayerDTO value) {
        return getMatchingLayer(value) != null;
    }

    private static UserLayerDTO getMatchingLayer(UserLayerDTO layerDTO) {
        if(userLayers != null) {
            for (UserLayerDTO layerDTOinList : userLayers) {
                if (layerDTOinList.getId().equals(layerDTO.getId())) {
                    return layerDTO;
                }
            }
        }
        return null;
    }

    public static void addLayers(List<UserLayerDTO> layers) {
        for(UserLayerDTO layerDTO : layers) {
            addLayer(layerDTO);
        }
        updateLayersTime();
    }

    private static void addLayer(UserLayerDTO layerDTO) {
        if(userLayers != null && getMatchingLayer(layerDTO) == null) {
            userLayers.add(layerDTO);
        }
    }

    public static boolean checkInfoVisibility() {
        for(UserLayerDTO layerDTOinList : userLayers) {
            if(layerDTOinList.isVisible())
                return true;
        }
        return false;
    }

    public static void removeLayer(UserLayerDTO layerDTO) {
        userLayers.remove(layerDTO);
    }

    public static void setLayersTime(Date currentTime) {
        UserLayersHelper.currentTime = currentTime;
        updateLayersTime();
    }

    private static void updateLayersTime() {
        if(userLayers == null || currentTime == null) {
            return;
        }
        for(UserLayerDTO userLayerDTO : userLayers) {
            if (userLayerDTO.getLayerDTO().isTimeEnabled()) {
                userLayerDTO.setSelectedTime(getNearestTime(userLayerDTO.getLayerDTO(), currentTime));
            }
        }
    }

    static private Date getNearestTime(LayerDTO layer, Date currentTime) {
        if(!layer.isTimeEnabled()) {
            return null;
        }
        Date nearest = layer.getDates().get(0);
        long minimum = Math.abs(currentTime.getTime() - nearest.getTime());
        for(Date date : layer.getDates()) {
            long value = Math.abs(currentTime.getTime() - date.getTime());
            if(value < minimum) {
                minimum = value;
                nearest = date;
            }
        }
        return nearest;
    }

}
