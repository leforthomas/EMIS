package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.entities.AOI;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.utils.ProductHelper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.earthimages.extapi.server.domain.ORBITDIRECTION;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.TYPE;
import com.metaaps.webapps.libraries.client.widget.IconAnchor;
import com.metaaps.webapps.libraries.client.widget.IconLabel;
import com.metaaps.webapps.libraries.client.widget.MessageLabel;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;
import com.metaaps.webapps.libraries.client.widget.util.Utils.FORMAT;

public abstract class ProductBaseWidget extends Composite {

	private static ProductBaseItemWidgetUiBinder uiBinder = GWT
			.create(ProductBaseItemWidgetUiBinder.class);

	interface ProductBaseItemWidgetUiBinder extends
			UiBinder<Widget, ProductBaseWidget> {
	}

	protected static StyleResources styleResources = GWT.create(StyleResources.class);

	public interface Style extends CssResource {
		String firstColumn();
		String secondColumn();
		String footerIcon();
        String propertiesHeader();
        String error();
        String highlighted();
        String optionsGrid();
		String dragOver();
	}
	
	// handlers for the various product request changes
	public static interface BaseProductChangeHandlers {
		void handleRemove();
		void toggleShow();
		void handleZoom();
		void toggleHighlighted();
	}

	@UiField
	protected Style style;

    @UiField
    protected HTMLPanel container;
	@UiField Image removeIcon;
	@UiField protected Label titleLabel;
	@UiField Image zoomIcon;
	@UiField Image displayIcon;
	@UiField
	protected HTMLPanel detailsPanel;
	@UiField(provided = true)
	protected ExpandWidget expandOrderingParameters;
	@UiField
	protected HTMLPanel orderParametersPanel;
	@UiField
	protected HTMLPanel productPropertiesPanel;
	@UiField
	protected Grid productProperties;
	@UiField HorizontalPanel bottomIcons;
	@UiField
	protected IconLabel estimatedPrice;
	@UiField
	protected FlowPanel footer;
    @UiField
    protected HTMLPanel labelPanel;
    @UiField
    protected HTMLPanel productSelectionExplanation;
    @UiField
    Image thumbnailImage;
    @UiField
    protected MessageLabel message;
    @UiField
    MessageLabel description;
    @UiField(provided = true)
    protected ExpandWidget expandProductProperties;
    @UiField
    HTMLPanel thumbnail;
    @UiField
    IconAnchor viewProduct;
	@UiField
	protected HTMLPanel additionalProductPropertiesControls;

    private static NumberFormat format = NumberFormat.getFormat("#.##");

    private BaseProductChangeHandlers baseProductChangeHandlers;

	public ProductBaseWidget() {

        expandOrderingParameters = new ExpandWidget(styleResources.arrowLeftSmall(), styleResources.arrowDownSmall());
        expandProductProperties = new ExpandWidget(styleResources.arrowLeftSmall(), styleResources.arrowDownSmall());

		initWidget(uiBinder.createAndBindUi(this));

		// initialise grid sizes
		productProperties.resize(0, 2);

		titleLabel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				if(baseProductChangeHandlers != null) {
					baseProductChangeHandlers.toggleHighlighted();
				}
			}
		});
		
	}

	protected void displayProductTitle(String title) {
		titleLabel.setText(title);
	}

	protected void displayProductProperties(Product product, AOI aoi) {
		productProperties.clear();
		productProperties.resizeRows(0);
		addTableProperty(productProperties, "Acquisition Start Date:", DateUtil.displayUTCMSDate(product.getStart()));
		addTableProperty(productProperties, "Acquisition Stop Date:", DateUtil.displayUTCMSDate(product.getStop()));
		addTableProperty(productProperties, "Orbit Direction:", product.getOrbitDirection() == null ? null : product.getOrbitDirection().toString());
        try {
            addTableProperty(productProperties, "Product area in sq kms:", Utils.formatSurface(FORMAT.SQKILOMETERS, 1, MapPanel.getPathArea(ProductHelper.getCoordinates(product))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(product.getSensorType().equalsIgnoreCase("OPTICAL")) {
            switch (product.getType()) {
                case ARCHIVE: {
                    addTableProperty(productProperties,"Image cloud cover", formatNumber(product.getCloudCoveragePercent() == null ? null : product.getCloudCoveragePercent() * 100, "%"));
                } break;
                case TASKING: {
                    if(product.getCloudCoveragePercent() != null) {
                        addTableProperty(productProperties, "Expected cloud cover", formatNumber(product.getCloudCoveragePercent() * 100, "%"));
                    } else if(product.getCloudCoverageStatisticsPercent() != null) {
                        addTableProperty(productProperties, "Statistical cloud cover", formatNumber(product.getCloudCoverageStatisticsPercent() * 100, "%"));
                    }
                } break;
            }
            addTableProperty(productProperties,"Off-Nadir Angle", formatNumber(product.getOna(), "deg"));
            addTableProperty(productProperties,"Illum. Angle", formatNumber(product.getSza(), "deg"));
            if(product.getStereoStackName() != null) {
                addTableProperty(productProperties,"Stereo", "Stack" + product.getStereoStackName());
            }
        } else {
            if(product.getOrbitDirection() != null) {
                addTableProperty(productProperties,"Orbit Dir.", formatString(product.getOrbitDirection() == ORBITDIRECTION.ASCENDING ? "Ascending" : "Descending"));
            }
            addTableProperty(productProperties,"Polarisation Mode", formatString(product.getPolarisation()));
            addTableProperty(productProperties,"Incidence Angle", formatNumber(product.getOza(), "deg"));
        }
/*
        addTableProperty(productProperties, "Useful Area (%):", NumberFormat.getFormat("0.00").format(product.getUsefulAreaPercent() * 100) + "%");
		addTableProperty(productProperties, "Observation Coverage (%):", NumberFormat.getFormat("0.00").format(product.getAoiCoveragePercent() * 100) + "%");
*/
/*
        boolean catalogueProduct = product.getType() == TYPE.ARCHIVE;
        viewProduct.setVisible(catalogueProduct);
        if(catalogueProduct) {
            viewProduct.setHref("#" + PlaceHistoryHelper.convertPlace(new ProductsPlace(Utils.generateTokens(ProductsPlace.TOKENS.productIds.toString(), product.getProductId() + ""))));
        }
*/
    }

    private String formatString(String value) {
        return value == null ?  "NA" : value;
    }

    private String formatNumber(Double value, String unitValue) {
        return value == null || value == -1 ?  "NA" : (format.format(value) + " " + unitValue);
    }

    protected void addTableProperty(Grid grid, String name, String value) {
		int index = grid.getRowCount();
		grid.resizeRows(index + 1);
		grid.setText(index, 0, name);
		grid.setText(index, 1, value);
		grid.getCellFormatter().setStyleName(index, 0, style.firstColumn());
		grid.getCellFormatter().setStyleName(index, 1, style.secondColumn());
	}

	protected void addTablePropertyWidget(Grid grid, String name, Widget widget) {
		int index = grid.getRowCount();
		grid.resizeRows(index + 1);
		grid.setText(index, 0, name);
		grid.setWidget(index, 1, widget);
		grid.getCellFormatter().setStyleName(index, 0, style.firstColumn());
		grid.getCellFormatter().setStyleName(index, 1, style.secondColumn());
	}

	protected void setBaseProductChangeHandlers(BaseProductChangeHandlers baseProductChangeHandlers) {
		this.baseProductChangeHandlers = baseProductChangeHandlers;
	}

	@UiHandler("removeIcon")
	void removeCartItem(ClickEvent event) {
		if(baseProductChangeHandlers != null) {
			baseProductChangeHandlers.handleRemove();
		}
	}
	
	@UiHandler("zoomIcon")
	void zoomCartItem(ClickEvent event) {
		if(baseProductChangeHandlers != null) {
			baseProductChangeHandlers.handleZoom();
		}
	}
	
	@UiHandler("displayIcon")
	void displayCartItem(ClickEvent event) {
		if(baseProductChangeHandlers != null) {
			baseProductChangeHandlers.toggleShow();
		}
	}
	
	public void displayShow(boolean show) {
		displayIcon.getElement().getStyle().setOpacity(show ? 1.0 : 0.3);
	}
	
	public void displayRemove(boolean display) {
		removeIcon.setVisible(display);
	}

	public void displayZoom(boolean visible) {
		zoomIcon.setVisible(visible);
	}
	
	public void setHighlighted(boolean highlighted) {
		if(highlighted) {
			thumbnail.addStyleName(style.highlighted());
		} else {
			thumbnail.removeStyleName(style.highlighted());
		}
	}
	
	public boolean isHighlighted() {
		return titleLabel.getStyleName().contains(style.highlighted());
	}
	
	protected void addFooterIcon(Widget iconWidget) {
		iconWidget.addStyleName(style.footerIcon());
		bottomIcons.add(iconWidget);
	}
	
	protected void addFooterWidget(Widget widget) {
		footer.add(widget);
	}

}
