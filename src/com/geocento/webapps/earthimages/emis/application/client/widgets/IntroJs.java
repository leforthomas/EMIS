package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.util.ResourcesLoader;

/**
 * Created by thomas on 17/09/2015.
 */
public class IntroJs {

    private static JSONArray steps = new JSONArray();

    public static void startTour() {
        new ResourcesLoader("introJs", new String[]{"./js/intro/introjs.min.css?1", "./js/intro/intro.js"}) {

            @Override
            protected void setLoading(String message) {

            }

            @Override
            protected void hideLoading() {

            }
        }.load(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void onSuccess(Void aVoid) {
                startTourJs(steps.getJavaScriptObject());
            }
        });
    }

    private static final native void startTourJs(JavaScriptObject steps) /*-{
        $wnd.introJs().setOption("steps", steps).start();
    }-*/;

    public static void clearSteps() {
        steps = new JSONArray();
    }

    public static void addStep(Widget widget, String message) {
        JSONObject step = new JSONObject();
        step.put("intro", new JSONString(message));
        if(widget != null) {
            step.put("element", new JSONObject(widget.getElement()));
        }
        steps.set(steps.size(), step);
    }

}
