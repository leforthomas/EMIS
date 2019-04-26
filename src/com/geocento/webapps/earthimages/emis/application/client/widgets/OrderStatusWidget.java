package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.share.entities.ORDER_STATUS;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
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

public class OrderStatusWidget extends Composite {

	private static OrderStatusWidgetUiBinder uiBinder = GWT
			.create(OrderStatusWidgetUiBinder.class);

	interface OrderStatusWidgetUiBinder extends
			UiBinder<Widget, OrderStatusWidget> {
	}
	
	protected static StyleResources styleResources = GWT.create(StyleResources.class);
	
	protected static ArrowedPopUpMenu popupMenu = null;
	
	@UiField protected HTMLPanel panel;

	protected ValueChangeHandler<ORDER_STATUS> statusChangeHandler;

	public OrderStatusWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	

	public void setStatus(final ORDER_STATUS status) {
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

	protected IconLabel getWidget(ORDER_STATUS status) {
		IconLabel widget = new IconLabel();
		String color = "#eee";
		switch(status) {

			case QUOTED: {
				widget.setText("Quoted");
				color = "#ec1";
			} break;
			case ACCEPTED: {
				widget.setText("Ordered");
				color = "#5a5";
			} break;
			case INPRODUCTION: {
				widget.setText("In Production");
				color = "#de1";
			} break;
			case DELIVERED: {
                widget.setText("Delivered");
                color = "#aca";
            } break;
			case COMPLETED: {
				widget.setText("Completed");
				color = "#aea";
			} break;
			case FAILED: {
				widget.setText("Failed");
				color = "#eaa";
			} break;
			case CANCELLED: {
				widget.setText("Cancelled");
				color = "#ea2";
			} break;
			case QUOTEREJECTED: {
				widget.setText("Rejected");
				color = "#eaf";
			} break;
			case ARCHIVED: {
				widget.setText("Archived");
				color = "#eee";
			} break;
			case REQUESTED: {
				widget.setText("Requested");
				color = "#eec";
			} break;
			default:
				return new ErrorIcon("Unknown Status");
		}
		widget.setResource(getStatusImageResource(status));
		// set the color
		panel.getElement().getStyle().setBackgroundColor(color);
		// add a cursor pointer if a status change action exists
		return widget;
	}
	
	protected ImageResource getStatusImageResource(ORDER_STATUS status) {
		switch(status) {
			case QUOTED:
				return styleResources.priceIcon();
			case ACCEPTED:
				return styleResources.validate();
			case INPRODUCTION:
				return com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.loading();
			case DELIVERED:
                return styleResources.checked();
			case COMPLETED:
				return styleResources.checked();
			case FAILED:
				return styleResources.alert();
			case CANCELLED:
				return styleResources.cancel();
			case REQUESTED:
				return styleResources.info();
			case ARCHIVED:
				return styleResources.cart();
			default:
				return styleResources.info();
		}
	}

	protected String getTooltip(ORDER_STATUS status) {
		String tooltip = null;
		switch(status) {
			case QUOTED: {
				tooltip = "Supplier has quoted the request";
			} break;
			case QUOTEREJECTED: {
				tooltip = "User rejected the quote";
			} break;
			case REQUESTED: {
				tooltip = "The products have been requested, awaiting supplier response";
			} break;
			case ACCEPTED: {
				tooltip = "User accepted and paid the quote for this order";
			} break;
			case INPRODUCTION: {
				tooltip = "Products have been ordered and/or are being generated";
			} break;
			case COMPLETED: {
				tooltip = "Order has been completed and products are available";
			} break;
			case FAILED: {
				tooltip = "Failed to produce the order";
			} break;
			case CANCELLED: {
				tooltip = "Supplier rejected the request";
			} break;
			case DELIVERED: {
                tooltip = "Products in the order have been delivered and are not downloadable anymore";
            } break;
			case ARCHIVED: {
				tooltip = "The order has been closed or archived by the user, no more products can be added";
			} break;
			default:
				break;
		}
		return tooltip;
	}

	protected List<ORDER_STATUS> getAuthorizedStatuses(ORDER_STATUS status) {
		List<ORDER_STATUS> statuses = new ArrayList<ORDER_STATUS>();
		switch (status) {
			case COMPLETED:
				statuses.add(ORDER_STATUS.ARCHIVED);
				break;
		}
		return statuses;
	}

	private void displayPopupMenu(ORDER_STATUS status) {
		if(statusChangeHandler == null) {
			return;
		}
		if(popupMenu == null) {
			popupMenu = new ArrowedPopUpMenu();
		}
		List<ORDER_STATUS> authorizedStatuses = getAuthorizedStatuses(status);
		if(authorizedStatuses.size() > 0) {
			popupMenu.clearItems();
			for(final ORDER_STATUS value : authorizedStatuses) {
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
	
	protected String convertStatusToActionString(ORDER_STATUS status) {
		switch(status) {
			case ACCEPTED:
				return "Products are being generated";
			case CANCELLED:
				return "Supplier rejected the request";
			case ARCHIVED:
				return "Archive order";
			case COMPLETED:
                return "Complete Order";
			default:
				return "Unknown";
		}
	}

	public void setStatusChangeHandler(ValueChangeHandler<ORDER_STATUS> statusChangeHandler) {
		this.statusChangeHandler = statusChangeHandler;
	}
	
}
