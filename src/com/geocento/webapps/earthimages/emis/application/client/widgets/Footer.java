package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;

public class Footer extends Composite {

	private static FooterUiBinder uiBinder = GWT.create(FooterUiBinder.class);
    @UiField
    SpanElement copyrightDates;
    @UiField
    AnchorElement contactUs;
    @UiField
    AnchorElement aboutUs;
    @UiField
    SpanElement applicationName;

    interface FooterUiBinder extends UiBinder<Widget, Footer> {
	}

	public interface Style extends CssResource {
		String linkSelected();
	}

    public Footer() {
		initWidget(uiBinder.createAndBindUi(this));
        applicationName.setInnerText(Application.getApplicationSettings().getApplicationName());
        copyrightDates.setInnerText("2011 - " + (1900 + new Date().getYear()));
        contactUs.setHref(Application.getApplicationSettings().getContactUs());
        aboutUs.setHref(Application.getApplicationSettings().getAboutUsURL());
	}

}
