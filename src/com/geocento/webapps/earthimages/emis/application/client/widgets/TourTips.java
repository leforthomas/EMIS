package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.ArrowedPopup;
import com.metaaps.webapps.libraries.client.widget.util.ResourcesLoader;
import com.metaaps.webapps.libraries.client.widget.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 17/09/2015.
 */
public class TourTips {

    static public class StepContent {
        Widget widget;
        Util.TYPE type;
        String content;

        public StepContent(Widget widget, Util.TYPE type,
                           String content) {
            super();
            this.widget = widget;
            this.type = type;
            this.content = content;
        }

    }

    private static List<StepContent> steps = new ArrayList<StepContent>();

    public static void startTour() {
        new ResourcesLoader("introJs", new String[]{"./js/intro/introjs.min.css?1"}) {

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
                showStep(0);
            }
        });
    }

    private static void showStep(final int index) {
        final ArrowedPopup popup = ArrowedPopup.getInstance();
        StepContent stepContent = steps.get(index);
        TourWidget tourWidget = new TourWidget();
        tourWidget.setContent(stepContent.content);
        tourWidget.getSkip().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popup.hide();
            }
        });
        tourWidget.getBack().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showStep(index - 1);
            }
        });
        tourWidget.getNext().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showStep(index + 1);
            }
        });
        tourWidget.setSteps(index, steps.size(), new TourWidget.StepSelectionHandler() {
            @Override
            public void onSelected(int index) {
                showStep(index);
            }
        });
        popup.showAt(stepContent.widget, tourWidget, stepContent.type);
    }

    public static void clearSteps() {
        steps.clear();
    }

    public static void addStep(Widget widget, Util.TYPE type, String message) {
        steps.add(new StepContent(widget, type, message));
    }

}
