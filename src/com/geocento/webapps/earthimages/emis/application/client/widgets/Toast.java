package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.metaaps.webapps.libraries.client.widget.util.Activity;
import com.metaaps.webapps.libraries.client.widget.util.ActivityCloseHandler;

/**
 * Created by thomas on 11/05/2017.
 */
public class Toast implements ResizeHandler {

    static private Toast instance = null;

    private PopupPanel popupPanel;
    private Timer timer;

    protected Toast() {
        popupPanel = new PopupPanel();
        popupPanel.removeStyleName("gwt-PopupPanel");
        popupPanel.getElement().getStyle().setZIndex(1000);
        popupPanel.setAnimationEnabled(true);
        Activity.addCloseHandler(new ActivityCloseHandler() {
            @Override
            public void onActivityClose() {
                if(timer != null) {
                    timer.cancel();
                    timer = null;
                }
                popupPanel.hide();
            }
        });
        Window.addResizeHandler(this);
    }

    public static Toast getInstance() {
        if(instance == null) {
            instance = new Toast();
        }
        return instance;
    }

    public void display(HTMLPanel panel, int duration) {
        popupPanel.clear();
        popupPanel.getElement().getStyle().setBackgroundColor("white");
        popupPanel.getElement().getStyle().setPadding(5, Style.Unit.PX);
        popupPanel.add(panel);
/*
        popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {

            }
        });
*/
        popupPanel.show();
        popupPanel.getElement().getStyle().clearTop();
        popupPanel.getElement().getStyle().clearLeft();
        popupPanel.getElement().getStyle().setBottom(10, Style.Unit.PX);
        popupPanel.getElement().getStyle().setRight(10, Style.Unit.PX);
        if(timer != null) {
            timer.cancel();
        }
        timer = new Timer() {
            @Override
            public void run() {
                popupPanel.hide();
                timer = null;
            }
        };
        timer.schedule(duration);
    }

    @Override
    public void onResize(ResizeEvent event) {

    }
}
