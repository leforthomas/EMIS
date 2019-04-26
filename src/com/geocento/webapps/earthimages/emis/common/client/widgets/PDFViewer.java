package com.geocento.webapps.earthimages.emis.common.client.widgets;

import com.geocento.webapps.earthimages.emis.common.client.utils.ScriptInjectors;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Created by thomas on 19/04/2016.
 */
public class PDFViewer extends Composite {

    interface CodeEditorUiBinder extends UiBinder<HTMLPanel, PDFViewer> {
    }

    private static CodeEditorUiBinder ourUiBinder = GWT.create(CodeEditorUiBinder.class);

    static public interface EventHandler {
        void onChange();
    }

    @UiField
    HTMLPanel panel;

    public PDFViewer() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void loadViewer(Callback<Void, Exception> callback) {
        panel.setVisible(false);
        ScriptInjectors.loadScripts(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(Void result) {
                panel.setVisible(true);
                callback.onSuccess(result);
            }
        }, "./js/pdf/pdf.js?new", "locale/locale.properties", "./js/pdf/viewer.js", "./js/pdf/viewer.css");
    }

    public static native void loadPDFFromUrl(String url) /*-{
        $wnd['PDFViewerApplication'].open(url);
    }-*/;

}