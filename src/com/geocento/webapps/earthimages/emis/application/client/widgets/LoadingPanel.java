package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.dom.client.Style.Unit;
import com.metaaps.webapps.libraries.client.widget.GlassPanel;
import com.metaaps.webapps.libraries.client.widget.IconLabel;

public class LoadingPanel extends GlassPanel {

    private static LoadingPanel instance;

    private IconLabel label;

	protected LoadingPanel() {
		super();
		label = new IconLabel(StyleResources.INSTANCE.loadingLarge(), "Loading...");
		label.getElement().getStyle().setFontSize(2, Unit.EM);
        label.getElement().getStyle().setPadding(20, Unit.PX);
		label.setSpacing(10);
		add(label);
	}

    static public LoadingPanel getInstance() {
        if(instance == null) {
            instance = new LoadingPanel();
        }
        return instance;
    }

    public void setText(String message) {
		label.setText(message);
	}

	public void show(String message) {
        show(message, false);
	}

    public void show(String message, boolean transparent) {
        label.getElement().getStyle().setBackgroundColor(transparent ? "transparent" : "#f0f0f0");
        setText(message);
        center();
        show();
    }
}
