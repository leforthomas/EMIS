package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.application.client.services.ProxyService;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.map.utils.GeometryUtils;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.util.ArrayList;
import java.util.List;

public class OGCHelper {

	static public List<String> supportedSRSs = ListUtil.toList(new String[] {"EPSG:3857", "EPSG:102100", "EPSG:900913"}); //, "CRS:84", "EPSG:4326"});

    protected static DateTimeFormat iso8601FMT = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.ISO_8601); //DateTimeFormat.getFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	protected String title;
    protected String description;
    protected String baseAddress;
    protected String version;
    protected String name;

	public OGCHelper() {
	}

	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getBaseAddress() {
		return baseAddress;
	}


	public void setBaseAddress(String baseAddress) {
		this.baseAddress = baseAddress;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    protected void callOWSServer(String service, String request, String parameters, final AsyncCallback<String> callBack) {
        String hostPageUrl = Window.Location.getHostName();
        boolean sameDomain = baseAddress.startsWith(hostPageUrl) || baseAddress.contains("//" + hostPageUrl);
        // call the getCapabilities via a proxy if the domain is different
        if(!sameDomain) {
            ProxyService.App.getInstance().proxyOWSRequest(baseAddress, service, request, parameters, callBack);
        } else {
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, baseAddress + "service=" + service + "&request=" + request + "&" + parameters);
            try {
                Request owsRequest = builder.sendRequest(null, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        callBack.onFailure(new Throwable("Couldn't retrieve Data"));
                    }

                    public void onResponseReceived(Request request, Response response) {
                        try {
                            if (200 == response.getStatusCode()) {
                                callBack.onSuccess(response.getText());
                            } else {
                                throw new Exception("Error querying the requested server: " + response.getStatusText());
                            }
                        } catch(Exception e) {
                            callBack.onFailure(new Throwable(e.getMessage()));
                        }
                    }

                });
            } catch (RequestException e) {
                callBack.onFailure(new Throwable("Couldn't retrieve Data"));
            }
        }
    }

    public static String toWMSBBox(EOBounds bbox, String srs, String version) throws Exception {
        double[] swCoordinates = null;
        double[] neCoordinates = null;
        switch (srs) {
            case "EPSG:3857":
                swCoordinates = GeometryUtils.convertTo("EPSG:3857", bbox.getCoordinatesSW());
                neCoordinates = GeometryUtils.convertTo("EPSG:3857", bbox.getCoordinatesNE());
                break;
            case "EPSG:4326":
                swCoordinates = new double[] {bbox.getCoordinatesSW().getLng(), bbox.getCoordinatesSW().getLat()};
                neCoordinates = new double[] {bbox.getCoordinatesNE().getLng(), bbox.getCoordinatesNE().getLat()};
                break;
        }
        // invert lat and long for version 1.3 and above
        if(version.startsWith("1.3")) {
            swCoordinates = new double[] {swCoordinates[1], swCoordinates[0]};
            neCoordinates = new double[] {neCoordinates[1], neCoordinates[0]};
        }
        return swCoordinates[0] + "," + swCoordinates[1] + "," + neCoordinates[0] + "," + neCoordinates[1];
    }

    protected static String getUniqueNodeValue(Node node, String name) {
        Node nodeValue = getUniqueNode(node, name);
        if(nodeValue == null) {
            return null;
        }
        if(nodeValue.getFirstChild() instanceof Text) {
            return ((Text) nodeValue.getFirstChild()).getData();
        } else {
            return "Unknown";
        }
    }

    private static String getNodeName(String nodeName, boolean withDomain) {
        return withDomain ? nodeName : stripDomain(nodeName);
    }

    protected static Node getUniqueNode(Node node, String name) {
        List<Node> nodes = getNodes(node, name, true);
        if(nodes.size() == 0) {
            return null;
        } else {
            return nodes.get(0);
        }
    }

    protected static List<Node> getNodes(Node node, String name) {
	    return getNodes(node, name, false);
    }

    protected static List<Node> getNodes(Node node, String name, boolean unique) {
        boolean withDomain = name.split(":").length > 1;
        if(node == null) {
            return null;
        }
        NodeList nodeList = node.getChildNodes();
        if(nodeList == null) {
            return null;
        }
        List<Node> nodes = new ArrayList<Node>();
        for(int index = 0; index < nodeList.getLength(); index++) {
            Node uniqueNode = nodeList.item(index);
            String nodeName = getNodeName(uniqueNode.getNodeName(), withDomain);
            if(uniqueNode != null && nodeName != null && nodeName.equalsIgnoreCase(name)) {
                nodes.add(uniqueNode);
                if(unique) {
                    break;
                }
            }
        }
        return nodes;
    }

    protected static List<String> getNodesValue(Node node, String name) {
        List<Node> nodes = getNodes(node, name);
        return ListUtil.mutate(nodes, new ListUtil.Mutate<Node, String>() {
            @Override
            public String mutate(Node nodeValue) {
                if(nodeValue.getFirstChild() instanceof Text) {
                    return (((Text) nodeValue.getFirstChild()).getData());
                } else {
                    return "Undefined";
                }
            }
        });
    }

    static protected String stripDomain(String value) {
        String[] values = value.split(":");
        return values.length == 1 ? values[0] : values[1];
    }

}
