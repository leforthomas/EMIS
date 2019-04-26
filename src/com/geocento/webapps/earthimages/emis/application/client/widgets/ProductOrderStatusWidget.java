package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.entities.PRODUCTORDER_STATUS;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.widget.ArrowedPopUpMenu;
import com.metaaps.webapps.libraries.client.widget.ErrorIcon;
import com.metaaps.webapps.libraries.client.widget.IconLabel;
import com.metaaps.webapps.libraries.client.widget.PopUpMenu.IPopupMenuEventListener;
import com.metaaps.webapps.libraries.client.widget.Tooltip;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;
import com.metaaps.webapps.libraries.client.widget.util.ValueChangeHandler;

import java.util.ArrayList;
import java.util.List;

public class ProductOrderStatusWidget extends Composite {

	private static OrderStatusWidgetUiBinder uiBinder = GWT
			.create(OrderStatusWidgetUiBinder.class);

	interface OrderStatusWidgetUiBinder extends
			UiBinder<Widget, ProductOrderStatusWidget> {
	}
	
	protected static StyleResources styleResources = GWT.create(StyleResources.class);
	
	protected static ArrowedPopUpMenu popupMenu = null;
	
	@UiField protected HTMLPanel panel;

	protected ValueChangeHandler<PRODUCTORDER_STATUS> statusChangeHandler;

	public ProductOrderStatusWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	protected List<PRODUCTORDER_STATUS> getAuthorizedStatuses(PRODUCTORDER_STATUS status) {
        List<PRODUCTORDER_STATUS> statuses = new ArrayList<PRODUCTORDER_STATUS>();
        switch (status) {
            case InProduction:
                statuses.add(PRODUCTORDER_STATUS.Cancelled);
                break;
        }
        return statuses;
	}

	public void setStatus(final PRODUCTORDER_STATUS status) {
		panel.clear();
		IconLabel widget = getWidget(status);
		String tooltip = getTooltip(status);
		// add a different widget depending on the status
		((IconLabel) widget).addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				displayPopupMenu(status);
			}

		});
		if(tooltip != null) {
			Tooltip.getTooltip().registerTooltip(widget, tooltip, TYPE.right);
		}
		panel.add(widget);
	}

	protected IconLabel getWidget(PRODUCTORDER_STATUS status) {
		IconLabel widget = new IconLabel();
		String color = "#eee";
		switch(status) {
            case Created: {
                widget.setText("Created");
                color = "#aea";
            } break;
            case Submitted: {
                widget.setText("Request submitted");
                color = "#de1";
            } break;
            case ChangeRequested: {
                widget.setText("Changes required");
                color = "#e84";
            } break;
            case Documentation: {
                widget.setText("License required");
                color = "#e84";
            } break;
            case DocumentationProvided: {
                widget.setText("License signed");
                color = "#5a5";
            } break;
			case Quoted: {
				widget.setText("Quoted");
				color = "#ec1";
			} break;
			case Accepted: {
				widget.setText("Ordered");
				color = "#5a5";
			} break;
			case Suspended: {
				widget.setText("Suspended");
				color = "#e71";
			} break;
			case InProduction: {
				widget.setText("In production");
				color = "#de1";
			} break;
            case Delivered: {
                widget.setText("Delivered");
                color = "#aca";
            } break;
			case Completed: {
				widget.setText("Completed");
				color = "#aea";
			} break;
			case Failed: {
				widget.setText("Failed");
				color = "#eaa";
			} break;
			case Cancelled: {
				widget.setText("Cancelled");
				color = "#eaa";
			} break;
			case Rejected: {
				widget.setText("Rejected");
				color = "#eaa";
			} break;
            case Downloading: {
                widget.setText("Downloading");
                color = "#aea";
            } break;
			default:
				return new ErrorIcon("Unknown Status");
		}
		widget.setResource(getStatusImageResource(status));
		// set the color
		panel.getElement().getStyle().setBackgroundColor(color);
		// add a cursor pointer if a status change action exists
		if(getAuthorizedStatuses(status).size() > 0) {
			widget.getElement().getStyle().setCursor(Cursor.POINTER);
		}
		return widget;
	}
	
	protected ImageResource getStatusImageResource(PRODUCTORDER_STATUS status) {
		switch(status) {
            case Created:
                return com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.loading();
            case ChangeRequested:
                return com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.warning();
			case Quoted:
				return styleResources.priceIcon();
/*
			case Accepted:
				return styleResources.validate();
*/
			case Suspended:
				return styleResources.error();
			case InProduction:
				return com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.loading();
/*
            case Submitted:
                return styleResources.importSmall();
            case Delivered:
                return styleResources.checked();
*/
            case Downloading:
                return com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.loading();
			case Completed:
				return styleResources.checked();
			case Failed:
				return styleResources.alert();
/*
			case Cancelled:
				return styleResources.cancel();
			case Rejected:
				return styleResources.cancel();
*/
			default:
				return null;
		}
	}

	protected String getTooltip(PRODUCTORDER_STATUS status) {
		String tooltip = null;
		switch(status) {
            case Created: {
                tooltip = "Request has been created, waiting for submission";
            } break;
            case Submitted: {
                tooltip = "Request has been submitted, waiting for confirmation";
            } break;
            case ChangeRequested: {
                tooltip = "The order desk has requested you changed the current selection, please see comments";
            } break;
			case Quoted: {
				tooltip = "Product has been confirmed and quoted, please accept or cancel the order.";
			} break;
			case Accepted: {
				tooltip = "You have ordered and paid for this product, awaiting product delivery.";
			} break;
			case Suspended: {
				tooltip = "Product order has been suspended by image supplier.";
			} break;
			case InProduction: {
				tooltip = "Product is currently being processed.";
			} break;
			case Completed: {
				tooltip = "Product has been completed and delivered";
			} break;
			case Failed: {
				tooltip = "Product order has failed, please check comments for further information.";
			} break;
			case Cancelled: {
				tooltip = "You have cancelled this product order.";
			} break;
			case Rejected: {
				tooltip = "The product order was rejected";
			} break;
            case Delivered: {
                tooltip = "The image supplier has delivered the product, if you specified internet delivery please use the product download URL";
            } break;
            case Downloading: {
                tooltip = "The product is being downloaded locally for publishing";
            } break;
            case Documentation:{
                tooltip = "You need to sign the user license agreement for this product";
            } break;
            case DocumentationProvided:{
                tooltip = "You have signed the license for this product, now waiting for final quote";
            } break;
			default:
				break;
		}
		return tooltip;
	}

	private void displayPopupMenu(PRODUCTORDER_STATUS status) {
		if(statusChangeHandler == null) {
			return;
		}
		if(popupMenu == null) {
			popupMenu = new ArrowedPopUpMenu();
		}
		List<PRODUCTORDER_STATUS> authorizedStatuses = getAuthorizedStatuses(status);
		if(authorizedStatuses.size() > 0) {
			popupMenu.clearItems();
			for(final PRODUCTORDER_STATUS value : authorizedStatuses) {
				popupMenu.addMenuItem(new IPopupMenuEventListener() {
					
					@Override
					public void execute() {
						statusChangeHandler.onValueChanged(value);
					}
				}, new IconLabel(getStatusImageResource(value), convertStatusToActionString(value)));
			}
			// make sure tooltip is hidden first
			Tooltip.getTooltip().hide();
			// display the popup menu
			popupMenu.showAt(panel, TYPE.right);
		}
	}
	
	protected String convertStatusToActionString(PRODUCTORDER_STATUS status) {
		switch(status) {
			case Accepted:
				return "Accept quote and pay";
			case Cancelled:
				return "Cancel Product Request";
			case Submitted:
				return "Validate Product";
            case Completed:
                return "Complete Order";
			default:
				return "Unknown";
		}
	}

	public void setStatusChangeHandler(ValueChangeHandler<PRODUCTORDER_STATUS> statusChangeHandler) {
		this.statusChangeHandler = statusChangeHandler;
	}
	
}
