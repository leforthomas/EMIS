package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.geocento.webapps.earthimages.emis.common.share.CoverageDTO;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.Feature;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;
import com.metaaps.webapps.libraries.client.map.EOBounds;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WCSCapabilities extends OGCHelper {

	private ArrayList<CoverageDTO> coveragesList;

	public WCSCapabilities() {
	}

	public WCSCapabilities(String baseUrl) {
	    this.baseAddress = baseUrl;
    }

    public ArrayList<CoverageDTO> getCoveragesList() {
        return coveragesList;
    }

    public void setCoveragesList(ArrayList<CoverageDTO> coveragesList) {
        this.coveragesList = coveragesList;
    }

    public void loadWCSCapabilities(String baseAddress, final AsyncCallback<Void> callBack) {
		this.baseAddress = baseAddress;
        callOWSServer("WCS", "GetCapabilities", null, new AsyncCallback<String>() {

            @Override
            public void onSuccess(String response) {
                try {
                    extractWCSXMLResources(response);
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

    private void extractWCSXMLResources(String response) throws Exception {
		// parse XML response
		Document document = XMLParser.parse(response);
		// get top element
		Element topNode = document.getDocumentElement();
		if(topNode.getNodeName().equalsIgnoreCase("ows:ExceptionReport")) {
			throw new EIException("Error querying service");
		}
		String topName = stripDomain(topNode.getNodeName());
		if(topName.contentEquals("Capabilities")) {
			version = topNode.getAttribute("version");
			Node serviceNode = getUniqueNode(topNode, "ServiceIdentification");
			title = getUniqueNodeValue(serviceNode, "Title");
			name = getUniqueNodeValue(serviceNode, "Name");
			description = getUniqueNodeValue(serviceNode, "Abstract");
			Node contentsNode = getUniqueNode(topNode, "Contents");
            List<Node> coverageNodes = getNodes(contentsNode, "CoverageSummary");
			coveragesList = new ArrayList<CoverageDTO>();
			if(coverageNodes != null && coverageNodes.size() > 0) {
			    for(Node coverageNode : coverageNodes) {
			        CoverageDTO coverageDTO = new CoverageDTO();
			        coverageDTO.setBaseUrl(baseAddress);
			        coverageDTO.setCoverageId(getUniqueNodeValue(coverageNode, "CoverageId"));
			        coverageDTO.setVersion(version);
			        coveragesList.add(coverageDTO);
                }
			}
			return;
		}
		throw new EIException("Unsupported protocol");
	}

    public void getCoverageDescription(String coverageId, final AsyncCallback<List<Feature>> callback) throws Exception {
        callOWSServer("WCS", "DescribeCoverage",
                "version=" + version +
                        "&coverageId=" + coverageId
                , new AsyncCallback<String>() {

                    @Override
                    public void onSuccess(String response) {
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        callback.onFailure(throwable);
                    }
                });
    }

    public String getDownloadCoverageUrl(String coverageId, EOBounds bounds, Date time) {
	    return getDownloadCoverageUrl(ListUtil.findValue(coveragesList, value -> value.getCoverageId().contentEquals(coverageId)), bounds, time);
    }

    public static String getDownloadCoverageUrl(CoverageDTO coverageDTO, EOBounds bounds, Date time) {
	    String requestUrl = coverageDTO.getBaseUrl() + "service=WCS&request=GetCoverage&version=" + coverageDTO.getVersion() + "&coverageId=" + coverageDTO.getCoverageId();
	    if(bounds != null) {
            requestUrl += "&SUBSET=Long(" + bounds.getCoordinatesSW().getLng() + "," + bounds.getCoordinatesNE().getLng() + ")&" +
                    "SUBSET=Lat(" + bounds.getCoordinatesSW().getLat() + "," + bounds.getCoordinatesNE().getLat() + ")";
            requestUrl += "&SUBSETTINGCRS=http://www.opengis.net/def/crs/EPSG/0/4326&";
        }
        if(time != null) {
            requestUrl += "&SUBSET=time(\"" + iso8601FMT.format(time, TimeZone.createTimeZone(0)) + "\")";
        }
        requestUrl += "&Format=geotiff";
	    return requestUrl;
    }
}
