package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.UnOrderedList;

/**
 * Created by thomas on 15/02/2016.
 */
public class TourWidget extends Composite {

    public static interface StepSelectionHandler {
        void onSelected(int index);
    }

    interface TourWidgetUiBinder extends UiBinder<Widget, TourWidget> {
    }

    private static TourWidgetUiBinder ourUiBinder = GWT.create(TourWidgetUiBinder.class);

    @UiField
    HTMLPanel content;
    @UiField
    Anchor skip;
    @UiField
    Anchor back;
    @UiField
    Anchor next;
    @UiField
    UnOrderedList listBullets;

    public TourWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setContent(String content) {
        this.content.add(new HTML(content));
    }

    public void setSteps(int index, int size, final StepSelectionHandler handler) {
        back.setStyleName("introjs-disabled", index == 0);
        next.setStyleName("introjs-disabled", index == size - 1);
        skip.setText(index < size - 1 ? "Skip" : "Done");
        listBullets.clear();
        for(int stepIndex = 0; stepIndex < size; stepIndex++) {
            Anchor stepAnchor = new Anchor("");
            if(stepIndex == index) {
                stepAnchor.setStyleName("active");
            } else {
                final int finalStepIndex = stepIndex;
                stepAnchor.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        handler.onSelected(finalStepIndex);
                    }
                });
            }
            listBullets.addListItem(stepAnchor);
        }
    }

    HasClickHandlers getSkip() {
        return skip;
    }

    HasClickHandlers getBack() {
        return back;
    }

    HasClickHandlers getNext() {
        return next;
    }

}