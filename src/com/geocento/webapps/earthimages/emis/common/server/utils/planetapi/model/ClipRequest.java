package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.model;

import java.util.List;

public class ClipRequest {

    public Aoi aoi;
    public List<Targets> targets;

    public static class Aoi {
        public String type;
        public double[][][] coordinates;
    }

    public static class Targets {
        public String item_id;
        public String item_type;
        public String asset_type;
    }

}
