package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.property.domain.TextProperty;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.ExpandWidget;
import com.metaaps.webapps.libraries.client.widget.IconLabel;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.Util;

import java.util.Iterator;
import java.util.List;

/**
 * Created by thomas on 11/02/2015.
 */
public class PackageWidget extends Composite implements HasWidgets {

    interface PackageWidgetUiBinder extends UiBinder<HTMLPanel, PackageWidget> {
    }

    private static PackageWidgetUiBinder ourUiBinder = GWT.create(PackageWidgetUiBinder.class);

    static public interface Style extends CssResource {
        String editable();

        String dragOver();
    }

    @UiField
    Style style;

    @UiField
    IconLabel title;
    @UiField
    ExpandWidget panel;
    @UiField
    HTMLPanel content;

    private CompletionHandler<String> completionHandler;

    public PackageWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));

        title.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(completionHandler != null) {
                    PopupPropertyEditor.getInstance().editAt(title, Util.TYPE.left, "Change the name of this package", null,
                            new TextProperty("Name", "Name for this package, must be unique.", title.getText(), true, 3, 100),
                            new CompletionHandler<List<Property>>() {
                                @Override
                                public void onCompleted(List<Property> result) throws ValidationException {
                                    completionHandler.onCompleted((String) result.get(0).getValue());
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                }
            }
        });

        panel.setExpanded(true);
    }

    public void setCompletionHandler(CompletionHandler<String> completionHandler) {
        this.completionHandler = completionHandler;
        title.addStyleName(style.editable());
    }

    public void setPackageName(String packageName) {
        title.setText(packageName);
    }

    public String getPackageName() {
        return title.getText();
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

    public void setDraggableInto() {

        this.addBitlessDomHandler(new DragOverHandler() {
            @Override
            public void onDragOver(DragOverEvent event) {
                event.preventDefault();
                String productId = event.getData("text");
                if(productId != null) {
                    content.addStyleName(style.dragOver());
                }
            }
        }, DragOverEvent.getType());

        this.addBitlessDomHandler(new DragLeaveHandler() {
            @Override
            public void onDragLeave(DragLeaveEvent event) {
                content.setStyleName(style.dragOver(), false);
            }
        }, DragLeaveEvent.getType());

        this.addBitlessDomHandler(new DropHandler() {
            @Override
            public void onDrop(DropEvent event) {
                event.preventDefault();
                String productId = event.getData("text");
                if(productId != null && productId.length() > 0) {
                    content.setStyleName(style.dragOver(), false);
                    //Application.clientFactory.getEventBus().fireEvent(new ProductOrderPackageChange(productId, getPackageName()));
                }
            }
        }, DropEvent.getType());

    }

}