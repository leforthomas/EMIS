package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.JavaScriptObject;

public class SignaturePad extends JavaScriptObject {

    protected SignaturePad() {
    }

    public final native void clearPad() /*-{
        this.clear();
    }-*/;

    public final native void undoPad() /*-{
        var data = this.toData();

        if (data) {
            data.pop(); // remove the last dot or line
            this.fromData(data);
        }
    }-*/;

}
