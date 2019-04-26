package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.*;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.EOLatLng;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.*;

public class WMSCapabilities extends OGCHelper {

	private ArrayList<LayerDTO> layersList;

	public WMSCapabilities() {
	}

	public WMSCapabilities(String baseUrl) {
	    this.baseAddress = baseUrl;
    }

	public ArrayList<LayerDTO> getLayersList() {
		return layersList;
	}

	public void setLayersList(ArrayList<LayerDTO> layersList) {
		this.layersList = layersList;
	}

	public void loadWMSCapabilities(String baseAddress, final AsyncCallback<Void> callBack) {
		this.baseAddress = baseAddress;
        callOWSServer("WMS", "GetCapabilities", null, new AsyncCallback<String>() {

            @Override
            public void onSuccess(String response) {
                try {
                    extractWMSXMLResources(response);
                } catch (Exception e) {
                    callBack.onFailure(new Throwable(e.getMessage()));
                    return;
                }
                callBack.onSuccess(null);
            }

            @Override
            public void onFailure(Throwable e) {
                callBack.onFailure(new Throwable(e.getMessage()));
            }
        });
	}

    private void extractWMSXMLResources(String response) throws Exception {
		// parse XML response
		Document document = XMLParser.parse(response);
		// get top element
		Element topElement = document.getDocumentElement();
		if(topElement.getNodeName().equalsIgnoreCase("ows:ExceptionReport")) {
			throw new EIException("Error querying service");
		}
		String topName = topElement.getNodeName();
		if(topName.contentEquals("WMS_Capabilities") || topElement.getNodeName().contentEquals("WMT_MS_Capabilities")) {
			version = topElement.getAttribute("version");
			Node serviceNode = Utils.getUniqueNode(topElement, "Service");
			title = Utils.getUniqueNodeValue(serviceNode, "Title");
			name = Utils.getUniqueNodeValue(serviceNode, "Name");
			description = Utils.getUniqueNodeValue(serviceNode, "Abstract");
			Node capabilityNode = Utils.getUniqueNode(topElement, "Capability");
			Node layers = Utils.getUniqueNode(capabilityNode, "Layer");
			layersList = new ArrayList<LayerDTO>();
			if(layers != null) {
				LayerDTO layer = new LayerDTO();
				layer.setLayerType(LAYER_TYPE.WMS);
				layer.setBaseUrl(baseAddress);
				layer.setVersion(version);
				parseXMLLayer(layers, layer);
			}
			return;
		}
		throw new EIException("Unsupported protocol");
	}

	private void parseXMLLayer(Node layerNode, LayerDTO parentLayer) throws Exception {
		LayerDTO layer = new LayerDTO();
		if(parentLayer != null) {
			layer.setBaseUrl(parentLayer.getBaseUrl());
			layer.setBounds(parentLayer.getBounds());
			layer.setDescription(parentLayer.getDescription());
			layer.setLayerType(parentLayer.getLayerType());
			// it is OK to do shallow copy as styles are immutable
            if(parentLayer.getStyles() != null) {
                for (LayerStyle style : parentLayer.getStyles()) {
                    layer.getStyles().add(style);
                }
            }
			layer.setSupportedSRS(parentLayer.getSupportedSRS());
			layer.setVersion(parentLayer.getVersion());
		}
		layer.setName(Utils.getUniqueNodeValue(layerNode, "Title"));
		layer.setLayerName(Utils.getUniqueNodeValue(layerNode, "Name"));
		layer.setDescription(Utils.getUniqueNodeValue(layerNode, "Abstract"));
        Node attributionNode = Utils.getUniqueNode(layerNode, "Attribution");
        if(attributionNode != null) {
            layer.setCredits(Utils.getUniqueNodeValue(attributionNode, "Title"));
        }
		// go for EX_GeographicBoundingBox values first
		if(Utils.getUniqueNode(layerNode, "EX_GeographicBoundingBox") != null) {
			try {
				Node boundBox = Utils.getUniqueNode(layerNode, "EX_GeographicBoundingBox");
				layer.setBounds(new EOBounds(
										new EOLatLng(Double.parseDouble(Utils.getUniqueNodeValue(boundBox, "northBoundLatitude")), Double.parseDouble(Utils.getUniqueNodeValue(boundBox, "eastBoundLongitude"))),
										new EOLatLng(Double.parseDouble(Utils.getUniqueNodeValue(boundBox, "southBoundLatitude")), Double.parseDouble(Utils.getUniqueNodeValue(boundBox, "westBoundLongitude")))));
			} catch(Exception e) {
			}
		} else if(Utils.getUniqueNode(layerNode, "LatLonBoundingBox") != null) {
				try {
					Element boundBox = (Element) Utils.getUniqueNode(layerNode, "LatLonBoundingBox");
					layer.setBounds(new EOBounds(
											new EOLatLng(Double.parseDouble(boundBox.getAttribute("maxy")), Double.parseDouble(boundBox.getAttribute("maxx"))),
											new EOLatLng(Double.parseDouble(boundBox.getAttribute("miny")), Double.parseDouble(boundBox.getAttribute("minx")))));
				} catch(Exception e) {
				}
		} else if(Utils.getUniqueNode(layerNode, "BoundingBox") != null) {
			try {
				// TODO - check the CRS/SRS
				Element boundBox = (Element) Utils.getUniqueNode(layerNode, "BoundingBox");
				layer.setBounds(new EOBounds(
										new EOLatLng(Double.parseDouble(boundBox.getAttribute("maxy")), Double.parseDouble(boundBox.getAttribute("maxx"))),
										new EOLatLng(Double.parseDouble(boundBox.getAttribute("miny")), Double.parseDouble(boundBox.getAttribute("minx")))));
			} catch(Exception e) {
			}
		}

		// check for dimensions
        List<Node> dimensionNodes = Utils.getNodes(layerNode, "Dimension");
        if(dimensionNodes != null && dimensionNodes.size() > 0) {
            Node timeNode = ListUtil.findValue(dimensionNodes, value -> {
                String nameValue = ((Element) value).getAttribute("name");
                return !StringUtils.isEmpty(nameValue) && nameValue.contentEquals("time");
            });
            if(timeNode != null) {
                List<Date> dates = new ArrayList<Date>();
                // assumes node value is always a list of comma separated strings in ISO8601 format
                if(timeNode.getFirstChild() instanceof Text) {
                    String timeNodeValue = ((Text) timeNode.getFirstChild()).getData();
                    for (String dateValue : timeNodeValue.split(",")) {
                        try {
                            Date date = iso8601FMT.parse(dateValue);
                            dates.add(date);
                        } catch (Exception e) {
                            com.geocento.webapps.earthimages.emis.common.client.utils.Utils.printLog("Error parsing layer date with value: " + dateValue);
                        }
                    }
                    // make sure they are ordered
                    Collections.sort(dates);
                    layer.setTimeEnabled(true);
                    layer.setDates(dates);
                }
            }
        }
		
		// assign an SRS
		List<String> listSRSs = Utils.getNodesValue(layerNode, version.startsWith("1.3") ? "CRS" : "SRS");
		if(listSRSs != null) {
			List<String> allSRSs = new ArrayList<String>();
			allSRSs.add(layer.getSupportedSRS());
			allSRSs.addAll(listSRSs);
			for(String supportedSRS : supportedSRSs) {
				if(allSRSs.contains(supportedSRS)) {
					layer.setSupportedSRS(supportedSRS);
					break;
				}
			}
		}
		
		// check for styles
		List<Node> styleNodes = Utils.getNodes(layerNode, "Style");
		ArrayList<LayerStyle> styles = layer.getStyles();
		if(styleNodes != null) {
			for(Node styleNode : styleNodes) {
				final String name = Utils.getUniqueNodeValue(styleNode, "Name");
				if(name != null && ListUtil.findIndex(styles, value -> value.getName() != null && name.contentEquals(value.getName())) == -1) {
					styles.add(new LayerStyle(name));
				}
			}
		}
		
		// parse layers
		List<Node> childrenNodes = Utils.getNodes(layerNode, "Layer");
		if(childrenNodes != null && childrenNodes.size() > 0) {
			for(Node childNode : childrenNodes) {
				parseXMLLayer(childNode, layer);
			}
		} else {
			// no more children layer so we can add it to the list
			layersList.add(layer);
		}
	}

    public void getFeatureInfo(String layers, String version, String styles, String srs, EOBounds bbox, int width, int height, int x, int y, final AsyncCallback<List<Feature>> callback) throws Exception {
        callOWSServer("WMS", "GetFeatureInfo",
                "version=" + version +
                        "&layers=" + layers +
                        "&styles=" + styles +
                        (version.startsWith("1.3") ? "&crs=" : "&srs=") + srs +
                        "&bbox=" + toWMSBBox(bbox, srs, version) +
                        "&width=" + width +
                        "&height=" + height +
                        "&query_layers=" + layers +
                        "&x=" + x +
                        "&y=" + y +
                        "&info_format=application/json"
                , new AsyncCallback<String>() {

                    @Override
                    public void onSuccess(String response) {
                        try {
                            JSONValue jsonResponse = JSONParser.parseLenient(response);
                            if(jsonResponse.isObject() != null && jsonResponse.isObject().containsKey("features")) {
                                callback.onSuccess(convertToFeatures(jsonResponse.isObject().get("features").isArray()));
                            } else {
                                throw new Exception("Problem parsing response");
                            }
                        } catch (Exception e) {
                            onFailure(e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        callback.onFailure(throwable);
                    }
                });
    }

    static private List<Feature> convertToFeatures(JSONArray featuresArray) {
        List<Feature> features = new ArrayList<Feature>();
        for (int index = 0; index < featuresArray.size(); index++) {
            JSONObject feature = featuresArray.get(index).isObject();
            if (feature != null && feature.get("type") != null && feature.get("type").isString() != null && feature.get("type").isString().stringValue().equalsIgnoreCase("Feature")) {
                String name = feature.get("id").toString();
                String wktGeometry = "";
                JSONObject geometry = feature.get("geometry").isObject();
                String geometryType = geometry.get("type").isString().stringValue().toLowerCase();
                // we don't support geometry collections for features
                if(geometryType.contentEquals("geometrycollection")) {
                    continue;
                }
                JSONArray coordinates = geometry.get("coordinates").isArray();
                if(geometryType.startsWith("multi")) {
                    // only take the first array in array of arrays
                    coordinates = coordinates.get(0).isArray();
                    geometryType = geometryType.replace("multi", "");
                }
                // convert coordinates from geojson to wkt
                String coordinatesString = geometryType.equalsIgnoreCase("polygon") ? coordinates.get(0).toString() : coordinates.toString();
                String wktCoordinates = coordinatesString.replace("\"", "").replace(",", " ").replace("[", "").replace("]", ",");
                wktCoordinates = StringUtils.stripEnd(wktCoordinates, ",");
                switch (geometryType) {
                    case "polygon":
                        wktGeometry = "POLYGON((" + wktCoordinates + "))";
                        break;
                    case "point":
                        wktGeometry = "POINT(" + wktCoordinates + ")";
                        break;
                    case "linestring":
                        wktGeometry = "LINESTRING(" + wktCoordinates + ")";
                        break;
                    // unsupported geometry type
                    default:
                        continue;
                }
                HashMap<String, String> propertiesMap = new HashMap<String, String>();
                JSONObject properties = feature.get("properties") != null && feature.get("properties").isObject() != null ? feature.get("properties").isObject() : null;
                for (String propertyName : properties.keySet()) {
                    propertiesMap.put(propertyName, properties.get(propertyName).toString());
                }
                features.add(new Feature(name, wktGeometry, propertiesMap));
            }
        }
        return features;
    }

}
