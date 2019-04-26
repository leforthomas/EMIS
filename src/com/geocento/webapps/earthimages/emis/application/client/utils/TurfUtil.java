package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;

public class TurfUtil {

    public static void loadTurf(Callback<Void, Exception> callback) {
        if(hasTurf()) {
            callback.onSuccess(null);
        } else {
            ScriptInjector.fromUrl("https://npmcdn.com/@turf/turf/turf.min.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(callback).inject();
        }
    }

    private static native boolean hasTurf() /*-{
        return $wnd['turf'];
    }-*/;
}
