package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.client.utils.Utils;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.place.SamplesPlace;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.utils.CartHelper;
import com.geocento.webapps.earthimages.emis.application.client.utils.PolicyHelper;
import com.geocento.webapps.earthimages.emis.application.client.utils.ProductDisplay;
import com.geocento.webapps.earthimages.emis.application.share.ProductOrderDTO;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.metaaps.webapps.earthimages.extapi.server.domain.ORBITDIRECTION;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.TYPE;
import com.metaaps.webapps.earthimages.extapi.server.domain.price.Price;
import com.metaaps.webapps.libraries.client.widget.Tooltip;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;

import java.util.List;

/**
 * Created by thomas on 12/09/2014.
 */
public class ProductCell extends AbstractCell<ProductDisplay> {

    /**
     * The HTML templates used to render the cell.
     */
    interface Templates extends SafeHtmlTemplates {
        /**
         * The template for this Cell, which includes styles and a value.
         */
        @Template("<div style='border: none; width: 200px; background: white; color: #333;'>" +
                "<div style='position: relative; height: 200px; line-height: 200px; padding: 0px; background-image: url(./img/loadingLarge.gif); background-position: center; background-size: contain; text-align: center; background-repeat: no-repeat; background-color: #eee;'>" +
                "<img style='width: 1px; height: 1px; vertical-align: middle; display: inline-block; margin: 0px;' onload='this.parentNode.style.backgroundImage = \"url(\" + this.getAttribute(\"src\") + \")\";' onerror='this.parentNode.style.backgroundImage = \"./img/no-image.png\"' src='{0}' alt='{7}'/>" +
                "<div style='position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px; padding: 10px; padding-top: 30px; background: white; background: rgba(255, 255, 255, 0.6); opacity: {5}; color: black; text-align: left; line-height: 1.2em; font-size: 0.9em;'>{6}</div>" +
                    "<div style='overflow: hidden; white-space: nowrap; text-align: center; padding: 5px; background: #79c8cf; position: absolute; top: 0; right: 0; display: {9}; line-height: normal;' title='{11}'>{10}</div>" +
                    "<div style='overflow: hidden; white-space: normal; text-align: center; padding: 10px; padding-top: 30px; background: white; opacity: 0.9; position: absolute; top: 0; right: 0; left: 0; display: {12}; line-height: normal; '>{13}</div>" +
                    "<div style='overflow: hidden; white-space: normal; text-align: center; padding: 10px; padding-top: 30px; background: white; opacity: 0.9; position: absolute; top: 0; right: 0; left: 0; display: {17}; line-height: normal; '>{18}</div>" +
                    "<div style='overflow: hidden; white-space: nowrap; text-align: center; padding: 5px; background: #93c022; position: absolute; top: 0; left: 0; display: {14}; line-height: normal;' title='{16}'>{15}</div>" +
                "</div>" +
                "</div>" +
                "<div style='background: #93c022; padding: 5px;'>" +
                "<div style='overflow: hidden; white-space: nowrap; text-align: center; padding: 5px; font-size: 0.9em;'>{1}</div>" +
                "<div style='overflow: hidden; white-space: nowrap; text-align: center; padding: 2px; font-size: 0.8em; color: white;'>{2}<span style='margin-left: 5px; background: white; color: black; border-radius: 3px; padding: 1px 2px;'>{8}</span></div>" +
                "<div style='overflow: hidden; white-space: nowrap; text-align: center; padding: 2px; font-size: 0.8em; color: white;'>{3}</div>" +
                "</div>" +
                "<div style='overflow: hidden; white-space: nowrap; text-align: center; padding: 5px; background: #79c8cf;'>{4}</div>" +
                "</div>")
        SafeHtml cell(String imageUrl, SafeHtml sensorDescription, String acquiredDate, SafeHtml bestPrice,
                      SafeHtml buttons,
                      // info panel
                      String opacity, SafeHtml values,
                      // information
                      String altTag, String resolution,
                      // strip panel
                      String stripVisible, SafeHtml scenesCount, String stripComment, String stripInfoVisible, SafeHtml stripInfo,
                      // order panel
                      String orderVisible, SafeHtml orderIcon, String orderComment, String orderInfoVisible, SafeHtml orderInfo
        );
    }

    interface NoActionImageTemplates extends SafeHtmlTemplates {
        @Template("<div title=\"{0}\" style=\"display:inline-block; cursor: default; padding: 5px;\">{1}</div>")
        SafeHtml cell(String title, SafeHtml value);
    }

    interface ImageTemplates extends SafeHtmlTemplates {
        @Template("<div name=\"{0}\" title=\"{1}\" style=\"{2}\">{3}</div>")
        SafeHtml cell(String name, String title, SafeStyles styles, SafeHtml value);
    }

    // generate the image cell
    protected SafeStyles firstImgStyle = SafeStylesUtils
            .fromTrustedString("display:inline-block; cursor:pointer; margin-left: -10px;");
    protected SafeStyles imgStyle = SafeStylesUtils
            .fromTrustedString("display:inline-block; cursor:pointer; padding: 0px 3px;");

    /**
     * Create a singleton instance of the templates used to render the cell.
     */
    private static Templates templates = GWT.create(Templates.class);
    private static NoActionImageTemplates noActionimageTemplates = GWT.create(NoActionImageTemplates.class);
    private static ImageTemplates imageTemplates = GWT.create(ImageTemplates.class);

    private static NumberFormat format = NumberFormat.getFormat("#.##");

    public ProductCell(Presenter presenter) {
        super("click", "mouseover", "mouseout");
        this.presenter = presenter;
    }

    static protected StyleResources styleResources = StyleResources.INSTANCE;

    static private enum ACTIONS {ADDCART, REMOVECART, DISPLAY, HIDE, ZOOM, OVERLAY, REMOVEOVERLAY, EXPORT, DOWNLOAD, OPEN, INFO, PRICEINFO, SHOW_STRIP, ADD_STRIP, SHOW_STRIPINFO, HIDE_STRIPINFO, SHOW_ORDERINFO, TOGGLE_PRODUCTORDER, HIDE_ORDERINFO, NOACTION};

    static public interface Presenter {

        void addToCart(ProductDisplay product);

        void removeFromcart(ProductDisplay product);

        void exportProduct(ProductDisplay product);

        void displayProductOverlay(ProductDisplay product, boolean display);

        void displayProductInMap(ProductDisplay value, boolean display);

        void downloadProduct(ProductDisplay value);

        void openProduct(ProductDisplay value);

        void zoomToProduct(ProductDisplay value);

        void showProductInfo(ProductDisplay value);

        void showProductPriceInfo(Element element, ProductDisplay value);

        void onMouseOver(Element element, ProductDisplay value);

        void onMouseOut(Element parent, ProductDisplay value);

        void showStrip(ProductDisplay productDisplay);

        void addStripToCart(ProductDisplay productDisplay);

        void showStripInfo(ProductDisplay productDisplay, boolean display);

        void showProductOrders(ProductDisplay productDisplay, boolean display);

        void showOrderInfo(ProductDisplay productDisplay, boolean display);
    }

    private static final SafeHtml ICON_SELECTED = makeImage(styleResources.checked());
    private static final SafeHtml ICON_UNSELECTED = makeImage(styleResources.unchecked());

    private static final SafeHtml ICON_CART = makeImage(styleResources.cartIcon());
    private static final SafeHtml ICON_UNCART = makeImage(styleResources.uncartIcon());
    private static final SafeHtml ICON_CART_GRAYED = makeImage(styleResources.cartGrayed());

    private static final SafeHtml ICON_EXPORT = makeImage(styleResources.export());

    private static final SafeHtml ICON_INFO = makeImage(styleResources.info());
    private static final SafeHtml ICON_INFO_ACTIVE = makeImage(styleResources.infoWhite());

    private static final SafeHtml ICON_DOWNLOAD = makeImage(styleResources.download());

    private static final SafeHtml ICON_OPENWINDOW = makeImage(styleResources.detach());

    private static final SafeHtml ICON_ZOOM = makeImage(styleResources.center());

    private static final SafeHtml ICON_OVERLAY_DISPLAYED = makeImage(styleResources.mapIconSmall());
    private static final SafeHtml ICON_OVERLAY_NOTDISPLAYED = makeImage(styleResources.mapIconNotDisplayed());
    private static final SafeHtml ICON_OVERLAY_LOADING = makeImage(com.metaaps.webapps.libraries.client.widget.style.StyleResources.INSTANCE.loading());

    private static final SafeHtml ICON_ORDER = makeImage(styleResources.orderIcon());

    private Presenter presenter;

    @Override
    public void render(Context context, ProductDisplay value, SafeHtmlBuilder sb) {

        SafeHtmlBuilder sbButtons = new SafeHtmlBuilder();

        Product product = value.getProduct();
        boolean archiveProduct = product.getType() == TYPE.ARCHIVE;

        sbButtons.append(value.isVisible() ? imageTemplates.cell(ACTIONS.HIDE.toString(), "Click to hide this product in the map", imgStyle, ICON_SELECTED) :
                imageTemplates.cell(ACTIONS.DISPLAY.toString(), "Click to display this product in the map", imgStyle, ICON_UNSELECTED));

        if(CartHelper.isDownloadable(product)) {
            sbButtons.append(imageTemplates.cell(ACTIONS.DOWNLOAD.toString(), "Click to download this product", imgStyle, ICON_DOWNLOAD));
        } else if(CartHelper.isOrderable(product)) {
            if (CartHelper.isProductInCart(product)) {
                sbButtons.append(imageTemplates.cell(ACTIONS.REMOVECART.toString(), "Click to remove this product from your selection", imgStyle, ICON_UNCART));
            } else {
                sbButtons.append(imageTemplates.cell(ACTIONS.ADDCART.toString(), "Click to add this product to your selection", imgStyle, ICON_CART));
            }
        } else {
            sbButtons.append(imageTemplates.cell(ACTIONS.NOACTION.toString(), "This product cannot be ordered, please contact us", imgStyle, ICON_CART_GRAYED));
        }

        if(archiveProduct) {
            if(value.isDisplayImage()) {
                if(value.isImageLoading()) {
                    sbButtons.append(imageTemplates.cell(ACTIONS.REMOVEOVERLAY.toString(), "This quicklook is loading. Click to stop loading the quicklook.", imgStyle, ICON_OVERLAY_LOADING));
                } else {
                    sbButtons.append(imageTemplates.cell(ACTIONS.REMOVEOVERLAY.toString(), "Click to remove the quicklook overlay on the map", imgStyle, ICON_OVERLAY_DISPLAYED));
                }
            } else {
                sbButtons.append(imageTemplates.cell(ACTIONS.OVERLAY.toString(), "Click to overlay the quicklook on the map, please be aware that it may take a few seconds for some images", imgStyle, ICON_OVERLAY_NOTDISPLAYED));
            }
        }

        sbButtons.append(imageTemplates.cell(ACTIONS.ZOOM.toString(), "Click to zoom to the product extent.", imgStyle, ICON_ZOOM));

        sbButtons.append(imageTemplates.cell(ACTIONS.EXPORT.toString(), "Click to export the product.", imgStyle, ICON_EXPORT));

        if(archiveProduct) {
            sbButtons.append(imageTemplates.cell(ACTIONS.OPEN.toString(), "Click to open product in new window.", imgStyle, ICON_OPENWINDOW));
        }

        sbButtons.append(imageTemplates.cell(ACTIONS.INFO.toString(),
                value.isShowInfo() ? "Click to hide product information" :"Click to view more info on this product.", imgStyle,
                value.isShowInfo() ? ICON_INFO_ACTIVE : ICON_INFO));

        String thumbnailUrl = product.getThumbnail() == null ?
                        "./img/no-image.png" :
                product.getThumbnail();
        Price bestPrice = value.getProduct().getConvertedSelectionPrice();
        SafeHtml priceInformation = SafeHtmlUtils.fromTrustedString(bestPrice == null ?
                "Price Not Available" : bestPrice.getValue() == 0 ?
                ("Free Product - delivery <a title='Typical time to fetch and publish the product'>" + PolicyHelper.getDeliveryTime(product) + "</a>") :
                ("Price: <a name='" + ACTIONS.PRICEINFO.toString() + "' " +
                        "title='Estimated best price based on public catalogue pricing and optimal scene selection'>" +
                        Utils.displayRoundedPrice(bestPrice) +
                        "</a>" +
                        " - delivery <a title='Typical delivery time from the moment of ordering, will be confirmed at the moment of ordering'>" + PolicyHelper.getDeliveryTime(product) + "</a>"
                        // TODO - add quick price policy explanation
/*
                        ProductHelper.
                        (value.getSpecificOrderPolicyDTO().getShapeSelectionPolicy() != null ?
                                (" (" + value.getSpecificOrderPolicyDTO().getShapeSelectionPolicy().getPricePerSqms().toString() + "/sqkms)") : "")
*/
                ));
        boolean hasStrip = value.getStripCount() > 0;
        int productOrdersCount = value.getProductOrders() == null ? 0 : value.getProductOrders().size();
        boolean hasOrder = productOrdersCount > 0;
        SafeHtml rendered = templates.cell(product.getType() == TYPE.ARCHIVE ?
                        thumbnailUrl :
                        "./img/planned-image-small.jpg",
                SafeHtmlUtils.fromTrustedString(
                        "<a style='display: block; max-width: 180px; overflow: hidden; text-overflow: ellipsis;' href='" +
                                product.getSensorInformationUrl() + "' target='_blank'>" + product.getSatelliteName() + " " + product.getInstrumentName() + "</a>"),
                product.getStart() != null ? DateUtil.displaySimpleUTCDate(product.getStart()) : "Unknown",
                priceInformation,
                sbButtons.toSafeHtml(),
                value.isShowInfo() ? "1.0" : "0.0",
                SafeHtmlUtils.fromTrustedString(value.isShowInfo() ? getProductInfo(value) : ""),
                "", //"Satellite image from " + product.getSatelliteName() + " provided by " + product.getProviderName(),
                product.getSensorResolution() + "m",
                hasStrip ? "block" : "none",
                SafeHtmlUtils.fromTrustedString("<a name='" + ACTIONS.SHOW_STRIPINFO.toString() + "'>" + value.getStripCount() + "</a>"),
                hasStrip ? "This product is a scene of a larger strip of " + value.getStripCount() + ". Click to see more options." : "",
                hasStrip && value.isShowStripInfo() ? "inline-block" : "none",
                SafeHtmlUtils.fromTrustedString(hasStrip && value.isShowStripInfo() ? getStripInfo(value) : ""),
                hasOrder ? "block" : "none",
                imageTemplates.cell(ACTIONS.SHOW_ORDERINFO.toString(), "Part of this product has already been ordered", imgStyle, ICON_ORDER),
                hasOrder ? "This product has been ordered previously. Click to see more information." : "",
                hasOrder && value.isShowOrderInfo() ? "inline-block" : "none",
                SafeHtmlUtils.fromTrustedString(hasOrder && value.isShowOrderInfo() ? getOrderInfo(value) : "")
                );

        sb.append(rendered);
    }

    private String getProductInfo(ProductDisplay productDisplay) {
        Product product = productDisplay.getProduct();
        String htmlContent = "";
        htmlContent += addProperty("Resolution", formatNumber(product.getSensorResolution(), "m"));
        htmlContent += addProperty("AoI Coverage", formatNumber(product.getAoiCoveragePercent() * 100, "%"));
        if(product.getOrbit() != null) {
            htmlContent += addProperty("Orbit", product.getOrbit());
        }
        if(product.getSensorType().equalsIgnoreCase("OPTICAL")) {
            htmlContent += addProperty("Cloud cover", formatNumber(product.getCloudCoveragePercent() == null ? null : product.getCloudCoveragePercent() * 100, "%"));
            htmlContent += addProperty("Off-Nadir Angle", formatNumber(product.getOna(), "deg"));
            htmlContent += addProperty("Illum. Angle", formatNumber(product.getSza(), "deg"));
            if(product.getStereoStackName() != null) {
                htmlContent += addProperty("Stereo", "Stack" + product.getStereoStackName());
            }
        } else {
            if(product.getOrbitDirection() != null) {
                htmlContent += addProperty("Orbit Dir.", formatString(product.getOrbitDirection() == ORBITDIRECTION.ASCENDING ? "Ascending" : "Descending"));
            }
            htmlContent += addProperty("Polarisation Mode", formatString(product.getPolarisation()));
            htmlContent += addProperty("Incidence Angle", formatNumber(product.getOza(), "deg"));
        }
        String sampleUrl = "./#" + PlaceHistoryHelper.convertPlace(new SamplesPlace(
                Utils.generateTokens(SamplesPlace.TOKENS.instrumentIds.toString(), productDisplay.getProduct().getInstrumentId() + "")
        ));
        htmlContent += "<div style='padding: 10px 0px;'>" +
                "There are samples for this product <a style='text-decoration: underline; margin-left: 10px;' href='" + sampleUrl + "' target='_blank'>View Samples</a>" +
                "</div>";
        return htmlContent;
    }

    private String getStripInfo(ProductDisplay productDisplay) {
        String htmlContent = "";
        // check if the product is part of a strip
        int stripCount = productDisplay.getStripCount();
        if(stripCount > 0) {
            htmlContent += "<p style='max-width: 100%;'>This product is part of a larger strip of " + stripCount + " products</p>";
            htmlContent += addProperty("Strip #", productDisplay.getProduct().getStrip());
            htmlContent += "<div style='text-align: right;'>" +
                    "<a name='" + ACTIONS.SHOW_STRIP.toString() + "' style='text-decoration: underline; margin-left: 10px;'>select strip</a>" +
                    "<a name='" + ACTIONS.ADD_STRIP.toString() + "' style='text-decoration: underline; margin-left: 10px;'>add to cart</a>" +
                    "</div>";
            htmlContent += "<p><a name='" + ACTIONS.HIDE_STRIPINFO.toString() + "' style='font-size: 0.8em; float: right;'>close</a><div style='clear: both;'></div></p>";
        }
        return htmlContent;
    }

    private String getOrderInfo(ProductDisplay productDisplay) {
        String htmlContent = "<div style='font-size: 0.9em; text-align: left;'>";
        // check if the product is part of a strip
        List<ProductOrderDTO> productOrders = productDisplay.getProductOrders();
        if(productOrders != null && productOrders.size() > 0) {
            int numberProductOrders = productOrders.size();
            htmlContent += "<p style='max-width: 100%;'>You have ordered this product " +
                    (numberProductOrders > 1 ? (numberProductOrders + " times ") : "") +
                    "in the past</p>";
            htmlContent += "<a name='" + ACTIONS.TOGGLE_PRODUCTORDER.toString() + "' style='text-decoration: underline; margin-left: 10px;'>" +
                    (productDisplay.isShowProductOrders() ? "hide on map" : "show on map") +
                    "</a>";
            htmlContent += "<p><a name='" + ACTIONS.HIDE_ORDERINFO.toString() + "' style='font-size: 0.8em; float: right;'>close</a><div style='clear: both;'></div></p>";
        }
        htmlContent += "</div>";
        return htmlContent;
    }

    private String formatString(String value) {
        return value == null ?  "NA" : value;
    }

    private String formatNumber(Double value, String unitValue) {
        return value == null || value == -1 ?  "NA" : (format.format(value) + " " + unitValue);
    }

    private String addProperty(String name, Object value) {
        return "<div><b>" + name + ": </b>" + (value == null ? "NA" : value.toString()) + "</div>";
    }

    /**
     * Called when an event occurs in a rendered instance of this Cell. The
     * parent element refers to the element that contains the rendered cell, NOT
     * to the outermost element that the Cell rendered.
     */
    @Override
    public void onBrowserEvent(Context context,
                               Element parent, ProductDisplay value, NativeEvent event,
                               ValueUpdater<ProductDisplay> valueUpdater) {

        // Let AbstractCell handle the keydown event.
        super.onBrowserEvent(context, parent, value, event, valueUpdater);

        // Ignore events that occur outside of the outermost element.
        EventTarget eventTarget = event.getEventTarget();

        if (parent.isOrHasChild(Element.as(eventTarget))) {
            // Handle the click event.
            if ("click".equals(event.getType())) {

                // if (parent.getFirstChildElement().isOrHasChild(
                // Element.as(eventTarget))) {

                // use this to get the selected element!!
                Element el = Element.as(eventTarget);

                // check if we really click on the image
                if (el.getNodeName().equalsIgnoreCase("IMG")) {
                    String action = el.getParentElement().getAttribute("name");
                    if(action == null) {
                        return;
                    }
                    performEvent(action, value, event);
                    doAction(value, valueUpdater);
                } else if (el.getNodeName().equalsIgnoreCase("A")) {
                    String action = el.getAttribute("name");
                    if (action == null || action.length() == 0) {
                        return;
                    }
                    performEvent(action, value, event);
                    doAction(value, valueUpdater);
                }
            } else if ("mouseover".equals(event.getType())) {
                presenter.onMouseOver(parent, value);
            } else if ("mouseout".equals(event.getType())) {
                presenter.onMouseOut(parent, value);
            }
        }

    };

    /**
     * Intern action
     *
     * @param value
     *            selected value
     * @param valueUpdater
     *            value updater or the custom value update to be called
     */
    private void doAction(ProductDisplay value, ValueUpdater<ProductDisplay> valueUpdater) {
        // Trigger a value updater. In this case, the value doesn't actually
        // change, but we use a ValueUpdater to let the app know that a value
        // was clicked.
        if (valueUpdater != null)
            valueUpdater.update(value);
    }

    protected void performEvent(String action, ProductDisplay productDisplay, NativeEvent event) {
        if("click".equals(event.getType())) {
            switch(ACTIONS.valueOf(action)) {
                case ADDCART:
                    presenter.addToCart(productDisplay);
                    break;
                case REMOVECART:
                    presenter.removeFromcart(productDisplay);
                    break;
                case OVERLAY:
                    presenter.displayProductOverlay(productDisplay, true);
                    break;
                case REMOVEOVERLAY:
                    presenter.displayProductOverlay(productDisplay, false);
                    break;
                case DISPLAY:
                    presenter.displayProductInMap(productDisplay, true);
                    break;
                case HIDE:
                    presenter.displayProductInMap(productDisplay, false);
                    break;
                case ZOOM:
                    presenter.zoomToProduct(productDisplay);
                    break;
                case EXPORT:
                    presenter.exportProduct(productDisplay);
                    break;
                case DOWNLOAD:
                    presenter.downloadProduct(productDisplay);
                    break;
                case OPEN:
                    presenter.openProduct(productDisplay);
                    break;
                case INFO:
                    presenter.showProductInfo(productDisplay);
                    break;
                case PRICEINFO:
                    presenter.showProductPriceInfo(Element.as(event.getEventTarget()), productDisplay);
                    break;
                case SHOW_STRIP:
                    presenter.showStrip(productDisplay);
                    break;
                case ADD_STRIP:
                    presenter.addStripToCart(productDisplay);
                    break;
                case SHOW_STRIPINFO:
                    presenter.showStripInfo(productDisplay, true);
                    break;
                case HIDE_STRIPINFO:
                    presenter.showStripInfo(productDisplay, false);
                    break;
                case TOGGLE_PRODUCTORDER:
                    presenter.showProductOrders(productDisplay, !productDisplay.isShowProductOrders());
                    break;
                case SHOW_ORDERINFO:
                    presenter.showOrderInfo(productDisplay, !productDisplay.isShowOrderInfo());
                    break;
                case HIDE_ORDERINFO:
                    presenter.showOrderInfo(productDisplay, false);
                    break;
            }
        // TODO - highlight product based on hover?
        } else if("mouseover".equals(event.getType())) {
        } else if("mouseout".equals(event.getType())) {
            Tooltip.getTooltip().hide();
        }
    }

    /**
     * Make icons available as SafeHtml
     *
     * @param resource
     * @return
     */
    protected static SafeHtml makeImage(ImageResource resource) {
        AbstractImagePrototype proto = AbstractImagePrototype.create(resource);

        return proto.getSafeHtml();
    }

}
