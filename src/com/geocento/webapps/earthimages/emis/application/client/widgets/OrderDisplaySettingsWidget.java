package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.MenuArrowedPanel;
import com.metaaps.webapps.libraries.client.widget.ValueSpinner;
import com.metaaps.webapps.libraries.client.widget.util.ValueChangeHandler;

/**
 * Created by thomas on 17/07/2014.
 */
public class OrderDisplaySettingsWidget extends Composite {

    private double overlayOpacity;

    interface OrderDisplaySettingsWidgetUiBinder extends UiBinder<Widget, OrderDisplaySettingsWidget> {
    }

    private static OrderDisplaySettingsWidgetUiBinder ourUiBinder = GWT.create(OrderDisplaySettingsWidgetUiBinder.class);

    @UiField
    MenuArrowedPanel displaySettings;
    @UiField(provided=true)
    protected ValueSpinner transparencyProductSelection;
    @UiField(provided=true)
    protected ValueSpinner transparencyOverlays;

    public OrderDisplaySettingsWidget() {

        transparencyProductSelection = new ValueSpinner(0, 0, 100);
        transparencyOverlays = new ValueSpinner(0, 0, 100);

        initWidget(ourUiBinder.createAndBindUi(this));

        // initialise the transparency values
        transparencyProductSelection.setValue((int) (0.3 * 100));
        transparencyOverlays.setValue((int) (0.9 * 100));
    }

    public void setImageResource(ImageResource imageResource) {
        displaySettings.setResource(imageResource);
    }

    public void setValueChangeHandler(ValueChangeHandler<Long> changeHandler) {
        transparencyProductSelection.setChangeHandler(changeHandler);
        transparencyOverlays.setChangeHandler(changeHandler);
    }

    public double getProductSelectionOpacity() {
        return transparencyProductSelection.getValue() / 100.0;
    }

    public double getOverlayOpacity() {
        return transparencyOverlays.getValue() / 100.0;
    }

    public void setProductSelectionOpacity(double productTransparency) {
        transparencyProductSelection.setValue((long) (productTransparency * 100));
    }

    public void setOverlayOpacity(double overlayOpacity) {
        transparencyOverlays.setValue((long) (overlayOpacity * 100));
    }

}