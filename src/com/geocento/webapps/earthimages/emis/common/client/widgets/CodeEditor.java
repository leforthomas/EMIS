package com.geocento.webapps.earthimages.emis.common.client.widgets;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Created by thomas on 19/04/2016.
 */
public class CodeEditor extends Composite implements HasValueChangeHandlers {

    interface CodeEditorUiBinder extends UiBinder<HTMLPanel, CodeEditor> {
    }

    private static CodeEditorUiBinder ourUiBinder = GWT.create(CodeEditorUiBinder.class);

    static public interface EventHandler {
        void onChange();
    }

    @UiField
    HTMLPanel panel;

    private JavaScriptObject aceEditor;

    public CodeEditor() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void loadEditor(Callback<Void, Exception> callback) {
        if(aceEditor != null) {
            callback.onSuccess(null);
            return;
        }
        panel.add(new Label("Loading..."));
        com.google.gwt.core.client.ScriptInjector.fromUrl("./js/ace/ace.js").setWindow(com.google.gwt.core.client.ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(Void result) {
                fixDefine();
                com.google.gwt.core.client.ScriptInjector.fromUrl("./js/ace/ext-language_tools.js").setWindow(com.google.gwt.core.client.ScriptInjector.TOP_WINDOW)
                        .setCallback(new Callback<Void, Exception>() {
                            @Override
                            public void onFailure(Exception reason) {
                                callback.onFailure(reason);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                aceEditor = startCodeEditing(panel.getElement(), "chrome", "python");
                                callback.onSuccess(result);
                            }
                        }).inject();
            }
        }).inject();

/*
        ScriptInjector.fromUrls(new Callback<Void, Exception>() {
            @Override
            public void onFailure(Exception reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(Void result) {
                aceEditor = startCodeEditing(panel.getElement(), "chrome", "python");
                callback.onSuccess(result);
            }
        },"./js/ace/ace.js", "./js/ace/ext-language_tools.js");
*/
    }

    public void startEditing(final String code, String mode) {
        aceEditor = startCodeEditing(panel.getElement(), "chrome", mode);
        setCodeEditing(aceEditor, code);
    }

    public String getCode() {
        return getCodeEditing(aceEditor);
    }

    private native final void fixDefine() /*-{
        var ace = $wnd['ace'];
        $wnd['define'] = ace.define;
    }-*/;

    private native final JavaScriptObject startCodeEditing(Element element, String theme, String mode) /*-{
        var ace = $wnd['ace'];
        $wnd['define'] = ace.define;
        ace.require("ace/ext/language_tools");
        var editor = ace.edit(element);
        editor.setTheme("ace/theme/" + theme);
        editor.getSession().setMode("ace/mode/" + mode);
        editor.setOptions({
            enableBasicAutocompletion: true,
            enableSnippets: true,
            enableLiveAutocompletion: false
        });
        editor.$blockScrolling = Infinity;
        return editor;
    }-*/;

    private native final String getCodeEditing(JavaScriptObject aceEditor) /*-{
        return aceEditor.getValue();
    }-*/;

    private native final void setCodeEditing(JavaScriptObject aceEditor, String code) /*-{
        aceEditor.setValue(code);
        aceEditor.clearSelection();
    }-*/;

    private native final void setOnEvent(JavaScriptObject aceEditor, String event, EventHandler eventHandler) /*-{
        var callback = eventHandler == null ? null : function(data) {
            eventHandler.@com.earthimages.publisher.processes.onlineeditor.common.client.widgets.CodeEditor.EventHandler::onChange()();
        }
        aceEditor.on(event, callback);
    }-*/;

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        setOnEvent(aceEditor, "change", new EventHandler() {
            @Override
            public void onChange() {
                handler.onValueChange(null);
            }
        });
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                setOnEvent(aceEditor, "change", null);
            }
        };
    }

}