package com.geocento.webapps.earthimages.emis.common.client.utils;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

public class ScriptInjectors {

    public static void loadScripts(Callback loadCallBack, String... libraries) {

        new Callback<Void, Exception>() {

            int index = 0;

            @Override
            public void onSuccess(Void arg0) {
                index++;
                if(index < libraries.length) {
                    loadNextLibrary();
                } else {
                    loadCallBack.onSuccess(null);
                }
            }

            public void loadNextLibrary() {
                String library = libraries[index];
                if(libraries[index].contains(".css")) {
                    Utils.injectCss(library);
                    onSuccess(null);
                } else if(libraries[index].contains(".properties")) {
                    injectProperties(library);
                    onSuccess(null);
                } else {
                    ScriptInjector.fromUrl(libraries[index]).setWindow(ScriptInjector.TOP_WINDOW).setCallback(this).inject();
                }
            }

            @Override
            public void onFailure(Exception arg0) {
                loadCallBack.onFailure(new Exception("Failed to load " + libraries[index]));
            }

        }.loadNextLibrary();
    }

    public static void injectProperties(String url) {
        LinkElement link = Document.get().createLinkElement();
        link.setRel("resource");
        link.setType("application/l10n");
        link.setHref(url);
        nativeAttachToHead(link);
    }

    protected static native void nativeAttachToHead(JavaScriptObject scriptElement) /*-{
        $doc.getElementsByTagName("head")[0].appendChild(scriptElement);
    }-*/;

}
