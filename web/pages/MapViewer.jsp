<%@ page import="com.geocento.webapps.earthimages.emis.common.server.utils.Utils" %>
<!DOCTYPE html>
<html>
<head>
    <title>Simple Map</title>
    <meta name="viewport" content="initial-scale=1.0">
    <meta charset="utf-8">
    <style>
        /* Always set the map height explicitly to define the size of the div
         * element that contains the map. */
        #map {
            height: 100%;
        }
        /* Optional: Makes the sample page fill the window. */
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
        }
    </style>

    <script src="https://maps.googleapis.com/maps/api/js?callback=initMap&key=AIzaSyB50v28QAm3etbTO_513E4AIPeaei2E0SM&libraries=geometry&language=en-GB"
            async defer></script>
    <script type="text/javascript" src="js/maps.js"></script>


</head>

<body>

    <div id="map"></div>

    <script type="text/javascript">

        var wmsUrl = <%= Utils.getSettings().getProductServiceWMSURL() %>;
        var workspace = <%= request.getParameter("workspace") %>;
        var layerName = <%= request.getParameter("layerId") %>;

        function initMap() {
            var zoomLevel = 7;
            var position = [10.0, 10.0];
            function start(library) {
                var map;
                var geodetic = true;
                var displayCoordinates = false;
                var displayGrid = false;
                var mapIndex = 0;
                var activateClipping = false;
                library.createMap(0, 0, 0, "hybrid", document.getElementById("map"),
                    function (map) {
                        var uniWMS = new uniWMSLayerClipped(map, wmsUrl, "1.1.1", workspace + ":" + layerName, layerDescription,
                            null, "EPSG:3857", extent.south, extent.west, extent.north, extent.east);
                        uniWMS.setVisible(true);
                        map.fitBounds(new gm.LatLngBounds(new gm.LatLng(extent.south, extent.east), new gm.LatLng(extent.north, extent.east)));
                    });
            }
        }

    </script>
  </body>
</html>

