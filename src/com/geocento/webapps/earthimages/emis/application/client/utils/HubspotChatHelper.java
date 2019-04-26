package com.geocento.webapps.earthimages.emis.application.client.utils;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ScriptElement;

public class HubspotChatHelper {

    static boolean injected = false;

    static public void injectScript() {
        if(!injected) {
            Element element = Document.get().getElementsByTagName("body").getItem(0);
            ScriptElement hs = Document.get().createScriptElement();
            hs.setSrc("//js.hs-scripts.com/2831686.js");
            hs.setId("hs-script-loader");
            //hs.setDefer("true");
            element.appendChild(hs);

/*
                    "<!-- Start of HubSpot Embed Code -->\n" +
                    "  <script type=\"text/javascript\" id=\"hs-script-loader\" async defer src=\"//js.hs-scripts.com/2831686.js\"></script>\n" +
                    "<!-- End of HubSpot Embed Code -->")
                    .setWindow(ScriptInjector.TOP_WINDOW)
                    .inject();
*/
            injected = true;
        }
    }

    static public native void displayChat(boolean display) /*-{
        $wnd['console'].log(display ? 'Display chat' : 'Hide chat');
        var chatWidget = $doc.getElementById("hubspot-messages-iframe-container");
        if(chatWidget) {
            chatWidget.style.display = display ? "initial" : "none !important";
        } else {
            $wnd['console'].log('Could not find the chat widget...');
        }
    }-*/;

}
