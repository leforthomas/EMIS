package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.util.ValueChangeHandler;

/**
 * Created by thomas on 17/07/2014.
 */
public class DisplaySettingsWidget extends Composite {

    interface DisplaySettingsWidgetUiBinder extends UiBinder<Widget, DisplaySettingsWidget> {
    }

    private static DisplaySettingsWidgetUiBinder ourUiBinder = GWT.create(DisplaySettingsWidgetUiBinder.class);

    @UiField
    MenuArrowedPanel displaySettings;
    @UiField(provided=true)
    protected ValueSpinner transparencyProduct;
    @UiField(provided=true)
    protected ValueSpinner transparencyObservation;
    @UiField(provided=true)
    protected ValueSpinner transparencyOverlays;
/*
    @UiField
    SelectedBox reduceDisplay;
*/

    public DisplaySettingsWidget() {

        transparencyProduct = new ValueSpinner(0, 0, 100);
        transparencyObservation = new ValueSpinner(0, 0, 100);
        transparencyOverlays = new ValueSpinner(0, 0, 100);

        initWidget(ourUiBinder.createAndBindUi(this));

        // initialise the transparency values
        transparencyProduct.setValue((int) (0.3 * 100));
        transparencyObservation.setValue((int) (0.3 * 100));
        transparencyOverlays.setValue((int) (0.9 * 100));
    }

    public void setResource(ImageResource imageResource) {
        displaySettings.setResource(imageResource);
    }

    public void setValueChangeHandler(final ValueChangeHandler<Long> changeHandler) {
        transparencyProduct.setChangeHandler(changeHandler);
        transparencyObservation.setChangeHandler(changeHandler);
        transparencyOverlays.setChangeHandler(changeHandler);
/*
        reduceDisplay.setSelectHandler(new SelectedBox.SelectHandler() {
            @Override
            public void onSelected(boolean selected) {
                changeHandler.onValueChanged(null);
            }
        });
*/
    }

    public double getProductOpacity() {
        return transparencyProduct.getValue() / 100.0;
    }

    public double getFeatureOpacity() {
        return transparencyObservation.getValue() / 100.0;
    }

    public double getOverlayOpacity() {
        return transparencyOverlays.getValue() / 100.0;
    }

/*
    public boolean isReducedDisplay() {
        return reduceDisplay.isSelected();
    }

*/
    public void setFeatureOpacity(double featureOpacity) {
        transparencyObservation.setValue((long) (featureOpacity * 100));
    }

    public void setProductOpacity(double productTransparency) {
        transparencyProduct.setValue((long) (productTransparency * 100));
    }

    public void setOverlayOpacity(double overlayOpacity) {
        transparencyOverlays.setValue((long) (overlayOpacity * 100));
    }

/*
    public void setReduceDisplay(boolean reduceDisplay) {
        this.reduceDisplay.setSelected(reduceDisplay);
    }
*/

}