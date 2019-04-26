package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.client.utils.ScriptInjectors;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

public class SignatureWidget extends Composite {

    interface SignatureWidgetUiBinder extends UiBinder<HTMLPanel, SignatureWidget> {
    }

    private static SignatureWidgetUiBinder ourUiBinder = GWT.create(SignatureWidgetUiBinder.class);

    public static interface Presenter {

        void handleValidate(String pngImageData);

    }

    @UiField(provided = true)
    Canvas canvas;
    @UiField
    Anchor validate;

    private Presenter presenter;

    private SignaturePad signaturePad;

    public SignatureWidget() {

        canvas = Canvas.createIfSupported();

        initWidget(ourUiBinder.createAndBindUi(this));

        ScriptInjectors.loadScripts(new Callback() {
            @Override
            public void onFailure(Object reason) {
                // TODO - show some message
            }

            @Override
            public void onSuccess(Object result) {
                signaturePad = startSignaturePad(canvas.getElement());
            }
        },"./js/signature/signature_pad.umd.js");
    }

    public void setPadWidth(int width) {
        canvas.setWidth(width + "px");
        canvas.setCoordinateSpaceWidth(width);
    }

    public void setPadHeight(int height) {
        canvas.setHeight(height + "px");
        canvas.setCoordinateSpaceHeight(height);
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    private final native SignaturePad startSignaturePad(Element canvas) /*-{
        return new $wnd['SignaturePad'](canvas, {
            // It's Necessary to use an opaque color when saving image as JPEG;
            // this option can be omitted if only saving as PNG or SVG
            backgroundColor: 'rgb(255, 255, 255)'
        });
    }-*/;

    @UiHandler("clear")
    void handleClear(ClickEvent clickEvent) {
        signaturePad.clearPad();
    }

    @UiHandler("undo")
    void handleUndo(ClickEvent clickEvent) {
        signaturePad.undoPad();
    }

    @UiHandler("validate")
    void handleValidate(ClickEvent clickEvent) {
        if(presenter != null) {
            presenter.handleValidate(canvas.toDataUrl("image/png"));
        }
    }

}