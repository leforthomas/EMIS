package com.geocento.webapps.earthimages.emis.common.share;

import java.util.HashMap;

public class Feature {
        String name;
        String wktGeometry;
        HashMap<String, String> properties;

        public Feature(String name, String wktGeometry, HashMap<String, String> properties) {
            this.name = name;
            this.wktGeometry = wktGeometry;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWktGeometry() {
            return wktGeometry;
        }

        public void setWktGeometry(String wktGeometry) {
            this.wktGeometry = wktGeometry;
        }

        public HashMap<String, String> getProperties() {
            return properties;
        }

        public void setProperties(HashMap<String, String> properties) {
            this.properties = properties;
        }
    }

