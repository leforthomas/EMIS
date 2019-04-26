<%@ page import="com.geocento.webapps.earthimages.emis.common.server.utils.Utils" %>
<%@ page import="com.geocento.webapps.earthimages.emis.common.server.utils.EMF" %>
<%@ page import="javax.persistence.EntityManager" %>
<%@ page import="com.geocento.webapps.earthimages.emis.application.client.widgets.PublishedLayerWidget" %>
<%@ page import="com.geocento.webapps.earthimages.emis.common.server.domain.PublishedLayer" %>
<!DOCTYPE html>
<html>
<%
    // get the layer id first
    String layerId = request.getParameter("layerId");
    if(layerId == null) {
        response.setStatus(404);
        return;
    }
    // find layer
    EntityManager em = EMF.get().createEntityManager();
    PublishedLayer publishedLayer = em.find(PublishedLayer.class, layerId);
    if(publishedLayer == null) {
        response.setStatus(404);
        return;
    }

    // get the basic information for the layer
    String workspace = publishedLayer.getPublishedWorkspace();
    String layerName = publishedLayer.getPublishedId();
    String name = publishedLayer.getName();
    String description = publishedLayer.getDescription();
    String extent = request.getParameter("extent");
%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">

    <meta name="viewport" content="initial-scale=1, maximum-scale=1,user-scalable=no">
    <title>Map with WMS</title>

    <link rel="stylesheet" href="https://js.arcgis.com/3.23/dijit/themes/claro/claro.css">
    <link rel="stylesheet" href="https://js.arcgis.com/3.23/esri/css/esri.css">
    <style>
        html, body, #map {
            height: 100%;
            width: 100%;
            margin: 0;
            padding: 0;
        }
        body {
            background-color: #FFF;
            overflow: hidden;
            font-family: "Trebuchet MS";
        }

        .name {
            font-weight: bold;
        }

        .description {
            color: #7a7a7a;
        }

    </style>
    <script src="https://js.arcgis.com/3.23/"></script>

    <script>
        var map;

        require(['esri/map', 'esri/layers/WMSLayer', 'esri/layers/WMSLayerInfo', 'esri/geometry/Extent',
                'dojo/_base/array', 'dojo/dom', 'dojo/dom-construct', 'dojo/parser',
                'dijit/layout/BorderContainer', 'dijit/layout/ContentPane', 'dojo/domReady!'],
            function(Map, WMSLayer, WMSLayerInfo, Extent, array, dom, domConst, parser) {

                parser.parse();

                esriConfig.defaults.io.proxyUrl = "/proxy/";

                var wmsUrl = '<%= Utils.getSettings().getProductServiceWMSURL().replace("$userName", workspace) %>';
                var layerName = '<%= layerName %>';
                var extent = <%= extent %>;

                map = new Map("map", {
                    basemap: "streets"
                });

                var bounds = new Extent(extent.west, extent.south, extent.east, extent.north, new esri.SpatialReference(4326));

                var wmsLayer = new WMSLayer(wmsUrl, {
                    format: "png",
                    resourceInfo: {
                        description: "Layer",
                        extent: bounds,
                        layerInfos: [
                            new WMSLayerInfo({
                                name: layerName
                            })
                        ],
                        spatialReferences: [3857],
                        version: "1.1.1"
                    },
                    version: "1.1.1",
                    visibleLayers: [layerName]
                });

                map.addLayer(wmsLayer);

                map.setExtent(bounds, true);

                var details = dom.byId('details');
                domConst.place('<p class="name"><%= name %></p>', details);
                domConst.place('<p class="description"><%= description %></p>', details);

            });
    </script>
</head>

<body class="claro">

    <div id="content"
         data-dojo-type="dijit.layout.BorderContainer"
         data-dojo-props="design:'headline', gutters:true"
         style="width: 100%; height: 100%; margin: 0;">

        <div id="details"
             data-dojo-type="dijit.layout.ContentPane"
             data-dojo-props="region:'left', splitter:true"
             style="overflow:auto; width:200px;">
        </div>

        <div id="map"
             data-dojo-type="dijit.layout.ContentPane"
             data-dojo-props="region:'center'"
             style="overflow:hidden;">
        </div>

    </div>

</body>
</html>
