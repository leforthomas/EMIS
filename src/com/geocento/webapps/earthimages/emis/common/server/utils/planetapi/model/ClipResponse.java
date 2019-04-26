package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.model;

import java.util.List;

public class ClipResponse {

    public _links _links;
    public Aoi aoi;
    public String created_on;
    public String id;
    public String last_modified;
    public String state;
    public List<Targets> targets;

    public static class _links {
        public String _self;
        public String[] results;
    }

    public static class Aoi {
        public double[][][] coordinates;
        public String type;
    }

    public static class Targets {
        public String asset_type;
        public String item_id;
        public String item_type;
    }
}
