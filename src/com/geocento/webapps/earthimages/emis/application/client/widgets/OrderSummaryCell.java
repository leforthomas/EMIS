package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.share.EventSummaryDTO;
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
import com.metaaps.webapps.libraries.client.widget.Tooltip;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;

/**
 * Created by thomas on 12/09/2014.
 */
public class OrderSummaryCell extends AbstractCell<EventSummaryDTO> {

    /**
     * The HTML templates used to render the cell.
     */
    interface Templates extends SafeHtmlTemplates {
        /**
         * The template for this Cell, which includes styles and a value.
         */
        @Template("<div style='position: relative; height: 80px; border: none; background: #e7ece3;'>" +
                "<div style='overflow: hidden; white-space: nowrap; text-align: left; padding: 5px; font-size: 1.1em; color: black;'>Name: {0}</div>" +
                "<div style='overflow: hidden; white-space: nowrap; text-align: left; padding: 5px; font-size: 0.9em; color: black; height: 1.4em; line-height: 1.4em; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>Description: {2}</div>" +
                "<div style='padding: 5px; font-size: 0.8em; color: black; overflow: hidden; white-space: nowrap; text-align: left;'>" +
                    "<div style='float: left;'>Number of products: {4}</div>" +
                    "<div style='float: left;  margin-left: 30px;'>Creation Date: {1}</div>" +
                    "<div style='float: left; margin-left: 30px;'>Status: {5}</div>" +
                "</div>" +
                "<div style='position:absolute;  z-index: 10; top:0px; width:80; right:0px; overflow: hidden; " +
                "white-space: nowrap; text-align: center; padding: 5px; background: #79c8cf;'>{3}</div>" +
                "</div>")
        SafeHtml cell(SafeHtml name, String creationDate, SafeHtml description, SafeHtml buttons, int numberOfProducts, String status);
    }

    interface ImageTemplates extends SafeHtmlTemplates {
        @Template("<div name=\"{0}\" title=\"{1}\" style=\"{2}\">{3}</div>")
        SafeHtml cell(String name, String title, SafeStyles styles, SafeHtml value);
    }

    // generate the image cell
  //  protected SafeStyles firstImgStyle = SafeStylesUtils
    //        .fromTrustedString("display:inline-block; cursor:pointer; margin-left: -10px;");
    protected SafeStyles imgStyle = SafeStylesUtils
            .fromTrustedString("display:inline-block; cursor:pointer; padding: 0px 3px;");

    /**
     * Create a singleton instance of the templates used to render the cell.
     */
    private static Templates templates = GWT.create(Templates.class);
    //private static NoActionImageTemplates noActionimageTemplates = GWT.create(NoActionImageTemplates.class);
    private static ImageTemplates imageTemplates = GWT.create(ImageTemplates.class);

    private static NumberFormat format = NumberFormat.getFormat("#.##");

    public OrderSummaryCell(Presenter presenter) {
        super("click", "mouseover", "mouseout");
        this.presenter = presenter;
    }

    static protected StyleResources styleResources = StyleResources.INSTANCE;

    static private enum ACTIONS {SELECT, OPEN, ZOOM, DELETE};

    static public interface Presenter {

        void onMouseOver(Element element, EventSummaryDTO value);

        void onMouseOut(Element parent, EventSummaryDTO value);

        void selectOrder(EventSummaryDTO eventSummaryDTO);

        void openOrder(EventSummaryDTO eventSummaryDTO);

        void zoomToOrder(EventSummaryDTO eventSummaryDTO);

        void deleteOrder(EventSummaryDTO eventSummaryDTO);
    }

    private static final SafeHtml ICON_DELETE = makeImage(styleResources.trashbinblue());

    private static final SafeHtml ICON_OPENWINDOW = makeImage(styleResources.detach());

    private static final SafeHtml ICON_ZOOM = makeImage(styleResources.center());

    private Presenter presenter;

    @Override
    public void render(Context context, EventSummaryDTO value, SafeHtmlBuilder sb) {

        SafeHtmlBuilder sbButtons = new SafeHtmlBuilder();

        sbButtons.append(imageTemplates.cell(ACTIONS.OPEN.toString(), "Click to open this order", imgStyle, ICON_OPENWINDOW));

        sbButtons.append(imageTemplates.cell(ACTIONS.ZOOM.toString(), "Click to zoom to the order extent.", imgStyle, ICON_ZOOM));

        sbButtons.append(imageTemplates.cell(ACTIONS.DELETE.toString(), "Click to delete this order.", imgStyle, ICON_DELETE));

        int numberOfProducts = value.getNumOfProducts();

        SafeHtml rendered = templates.cell(SafeHtmlUtils.fromTrustedString(value.getName()),
                value.getCreationDate() != null ? DateUtil.displaySimpleUTCDate(value.getCreationDate()) : "Unknown",
                SafeHtmlUtils.fromTrustedString(value.getDescription() == null ? "no description provided" : value.getDescription()), //priceInformation,
                sbButtons.toSafeHtml(),
                numberOfProducts,
                value.getStatus().toString());

        sb.append(rendered);

    }

    private String getOrderInfo(EventSummaryDTO eventSummaryDTO) {
        String htmlContent = "";
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
                               Element parent, EventSummaryDTO value, NativeEvent event,
                               ValueUpdater<EventSummaryDTO> valueUpdater) {

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
                else if (el.getNodeName().equalsIgnoreCase("DIV"))
                {
                    String action = ACTIONS.SELECT.toString();
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
    private void doAction(EventSummaryDTO value, ValueUpdater<EventSummaryDTO> valueUpdater) {
        // Trigger a value updater. In this case, the value doesn't actually
        // change, but we use a ValueUpdater to let the app know that a value
        // was clicked.
        if (valueUpdater != null)
            valueUpdater.update(value);
    }

    protected void performEvent(String action, EventSummaryDTO eventSummaryDTO, NativeEvent event) {
        if("click".equals(event.getType())) {
            switch(ACTIONS.valueOf(action)) {
                case SELECT:
                    presenter.selectOrder(eventSummaryDTO);
                    break;
                case OPEN:
                    presenter.openOrder(eventSummaryDTO);
                    break;
                case DELETE:
                    presenter.deleteOrder(eventSummaryDTO);
                    break;
                case ZOOM:
                    presenter.zoomToOrder(eventSummaryDTO);
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
