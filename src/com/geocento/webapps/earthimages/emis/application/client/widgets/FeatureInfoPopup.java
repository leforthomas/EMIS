package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.application.client.event.AddAoIImported;
import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.share.Feature;
import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.application.client.utils.AOIUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.ArrowedPopUpMenu;
import com.metaaps.webapps.libraries.client.widget.IconAnchor;
import com.metaaps.webapps.libraries.client.widget.MessageLabel;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.metaaps.webapps.libraries.client.widget.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by thomas on 06/11/2014.
 */
public class FeatureInfoPopup implements ShowRangeHandler<Integer> {

    static public interface Presenter {
        void onImportFeature(Feature feature);
    }

    interface Style extends CssResource
    {

        String buttons();

        String values();

        String portlet();

        String message();

        String valuesPanel();

        String panel();
    }

    interface FeatureInfoPopupUiBinder extends UiBinder<ArrowedPopUpMenu, FeatureInfoPopup>
    {
    }

    private static FeatureInfoPopupUiBinder ourUiBinder = GWT.create(FeatureInfoPopupUiBinder.class);

    private static FeatureInfoPopup instance;

    @UiField
    NavigationPortlet portlet;
    @UiField
    Grid propertiesGrid;
    @UiField
    MessageLabel message;
    @UiField
    IconAnchor importFeature;

    private final ArrowedPopUpMenu popup;

    private Presenter presenter;

    private List<Feature> features = new ArrayList<Feature>();

    public FeatureInfoPopup() {
        popup = ourUiBinder.createAndBindUi(this);
        portlet.addNavigationToolbar(1, 10);
        portlet.addShowRangeHandler(this);
    }

    public static FeatureInfoPopup getInstance() {
        if(instance == null) {
            instance = new FeatureInfoPopup();
        }
        return instance;
    }

    public void showFeatures(List<Feature> features) {
        showMessage(false);
        this.features.clear();
        this.features.addAll(features);
        portlet.setMaxCount(features.size());
        displayIndex(0);
    }

    public void showAt(Widget widget, Util.TYPE type) {
        popup.showAt(widget, type);
    }

    public void setLoading(String message) {
        portlet.setVisible(false);
        this.message.displayLoading(message);
    }

    public void hideLoading() {
        message.setVisible(false);
    }

    public void displayMessage(String message) {
        showMessage(true);
        this.message.displayInfo(message);
    }

    private void showMessage(boolean show) {
        portlet.setVisible(!show);
        message.setVisible(show);
    }

    @Override
    public void onShowRange(ShowRangeEvent<Integer> event) {
        displayIndex(event.getStart());
    }

    private void displayIndex(int index) {
        portlet.setIndex(index);
        Feature feature = features.get(index);
        portlet.setTitle("Feature " + feature.getName());
        propertiesGrid.clear();
        // add the property values
        Set<String> properties = feature.getProperties().keySet();
        propertiesGrid.resize(properties.size(), 2);
        int i = 0;
        for(String propertyName : properties) {
            propertiesGrid.setText(i, 0, propertyName);
            addValue(i, convertText(feature.getProperties().get(propertyName)));
            i++;
        }
    }

    private void addValue(int row, String value) {
        if(value.startsWith("http")) {
            Anchor valueLink = new Anchor("link");
            valueLink.setHref(value);
            valueLink.setTarget("_blank");
            valueLink.addStyleName(StyleResources.INSTANCE.style().eiBlueAnchor());
            propertiesGrid.setWidget(row, 1, valueLink);
        } else {
            propertiesGrid.setText(row, 1, value);
        }
    }

    private String convertText(String value) {
        if(StringUtils.isEmpty(value) || value.contentEquals("null")) {
            return "Not available";
        }
        if(value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    @UiHandler("importFeature")
    void importFeature(ClickEvent clickEvent) {
        try {
            Feature feature = features.get(portlet.getIndex());
            AOI aoi = AOIUtils.fromWKT(feature.getWktGeometry());
            aoi.setName(feature.getName());
            Application.clientFactory.getEventBus().fireEvent(new AddAoIImported(aoi));
        } catch (Exception e) {
            Window.alert("Error with features geometry, could not import feature as AoI.");
        }
    }

}