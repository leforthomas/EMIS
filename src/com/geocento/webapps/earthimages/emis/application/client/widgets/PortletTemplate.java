package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.ClientFactory;
import com.metaaps.webapps.libraries.client.widget.IconLabel;

import java.util.Iterator;

public class PortletTemplate extends Composite implements HasWidgets
{

	private static PortletTemplateUiBinder uiBinder = GWT
			.create(PortletTemplateUiBinder.class);

	interface PortletTemplateUiBinder extends UiBinder<Widget, PortletTemplate>
    {
	}
	
	StyleResources styleResources = GWT.create(StyleResources.class);
	
	@UiField
    Panel panel;
	@UiField
    SpanElement title;
	@UiField
	protected HTMLPanel toolbar;
	@UiField
    IconLabel message;
	@UiField
    FlowPanel contentPanel;
	@UiField
	protected FlowPanel footer;
    @UiField
    HTMLPanel titlePanel;

    private boolean loading;
	
	public PortletTemplate() {
		initWidget(uiBinder.createAndBindUi(this));
		message.setVisible(false);
	}
	
	public void setTitle(String title) {
		this.title.setInnerText(title);
	}
	
	@UiChild(tagname="toolbar", limit = 1)
	public void addToolbar(Widget widget) {
		toolbar.clear();
		toolbar.add(widget);
	}
	
	public void setLoading(boolean loading) {
		this.loading = loading;
		if(loading) {
			message.setResource(ClientFactory.getStyleResources().loading());
			message.setText("Loading...");
			message.setVisible(true);
		} else {
			message.setVisible(false);
		}
	}
	
	public boolean isLoading() {
		return loading;
	}
	
	public void setSpacing(int spacing) {
		contentPanel.getElement().getStyle().setPadding(spacing, Unit.PX);
	}
	
	public void setInfoMessage(String message) {
		this.message.setResource(styleResources.info());
		this.message.setText(message);
		this.message.setVisible(true);
	}
	
	public void setErrorMessage(String message) {
		this.message.setResource(styleResources.error());
		this.message.setText(message);
		this.message.setVisible(true);
	}
	
	public void hideMessage() {
		this.message.setVisible(false);
	}

    public void setTitleStyleName(String styleName, boolean add) {
        titlePanel.setStyleName(styleName, add);
    }
	
	@Override
	public void addStyleName(String style) {
		panel.addStyleName(style);
	}
	
	@Override
	public void add(Widget w) {
		contentPanel.add(w);
	}

	@Override
	public void clear() {
		contentPanel.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return contentPanel.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return contentPanel.remove(w);
	}

}
