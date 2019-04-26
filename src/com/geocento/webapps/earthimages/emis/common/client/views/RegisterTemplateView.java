package com.geocento.webapps.earthimages.emis.common.client.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.Iterator;

public class RegisterTemplateView extends Composite implements HasWidgets {

	private static EIRegisterTemplateViewUiBinder uiBinder = GWT
			.create(EIRegisterTemplateViewUiBinder.class);

	interface EIRegisterTemplateViewUiBinder extends
			UiBinder<Widget, RegisterTemplateView> {
	}
	
	@UiField FlowPanel content;
	@UiField
	HTMLPanel footerHolder;
    @UiField
    HTMLPanel centerFrame;
    @UiField
    HTMLPanel panel;

    public RegisterTemplateView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

    public void setBackgroundImage(String imageUrl) {
        panel.getElement().getStyle().setBackgroundImage(imageUrl);
        panel.getElement().getStyle().setProperty("backgroundSize", "100%");
    }

	@UiChild(tagname = "footer")
	public void setFooter(Widget widget) {
		footerHolder.clear();
		footerHolder.add(widget);
	}

	public void setCenterFrameStyleName(String styleName) {
		centerFrame.addStyleName(styleName);
	}

	@Override
	public void add(Widget w) {
		content.add(w);
	}

	@Override
	public void clear() {
		content.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return content.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return content.remove(w);
	}

}
