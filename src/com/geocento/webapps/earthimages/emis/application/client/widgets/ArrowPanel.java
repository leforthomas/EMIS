package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.metaaps.webapps.libraries.client.widget.util.ActivityCloseHandler;

/**
 * Created by thomas on 21/03/2016.
 */
public class ArrowPanel implements ActivityCloseHandler {

    private static ArrowPanel instance;

    private final HTMLPanel panel;

    protected ArrowPanel() {
        panel = new HTMLPanel("");
        Document.get().getBody().appendChild(panel.getElement());
        panel.getElement().getStyle().setProperty("pointerEvents", "none");
    }

    static public ArrowPanel getInstance() {
        if(instance == null) {
            instance = new ArrowPanel();
        }
        return instance;
    }

    public void displayAt(boolean horizontal, double fromX, double fromY, double toX, double toY, String color) {
        if(!supportsSVG()) {
            return;
        }
        panel.clear();
        int margin = 10, width = 0, height = 0, top = 0, left = 0;
        String path = "";
        Style panelStyle = panel.getElement().getStyle();
        panelStyle.setPosition(Style.Position.FIXED);
        if(horizontal) {
            top = (int) (Math.min(fromY, toY));
            left = (int) (Math.min(fromX, toX) - margin);
            width = (int) Math.abs(fromX - toX) + 2 * margin;
            height = (int) Math.abs(fromY - toY) + 2;
            boolean leftToRight = fromX - toX < 0;
            boolean topToBottom = fromY - toY < 0;
            if (leftToRight) {
                if (topToBottom) {
                    path = "M0,1 L" + (2 * margin) + ",1 M" + margin + ",1 L" + (width - margin) + "," + (height - 1);
                } else {
                    path = "M0," + (height - 1) + " L" + (2 * margin) + "," + (height - 1) +
                            " M" + margin + "," + (height - 1) + " L" + (width - margin) + ",1";
                }
            } else {
                if (topToBottom) {
                    path = "M" + width + ",1 L" + (width - 2 * margin) + ",1 M" + (width - margin) + ",1 L" + margin + "," + (height - 1);
                } else {
                    path = "M" + width + "," + (height - 1) + " L" + (width - 2 * margin) + "," + (height - 1) +
                            " M" + (width - margin) + "," + (height - 1) + " L" + margin + ",1";
                }
            }
        } else {
            top = (int) (Math.min(fromY, toY) - margin);
            left = (int) Math.min(fromX, toX);
            width = (int) Math.abs(fromX - toX) + 2;
            height = (int) Math.abs(fromY - toY) + 2 * margin;
            boolean leftToRight = fromX - toX < 0;
            boolean topToBottom = fromY - toY < 0;
            if (leftToRight) {
                if (topToBottom) {
                    path = "M1,0 L1,20 M1,10 L" + (width - 1) + "," + (height - margin);
                } else {
                    path = "M1," + height + " L1," + (height - 2 * margin) +
                            " M1," + (height - margin) + " L" + (width - 1) + "," + margin;
                }
            } else {
                if (topToBottom) {
                    path = "M" + width + ",0 L" + width + ",20 M" + width + ",10 L0," + (height - margin);
                } else {
                    path = "M" + width + "," + height + " L" + width + "," + (height - 2 * margin) +
                            " M" + width + "," + (height - margin) + " L0,0";
                }
            }
        }
        panelStyle.setWidth(width, Style.Unit.PX);
        panelStyle.setHeight(height, Style.Unit.PX);
        panelStyle.setTop(top, Style.Unit.PX);
        panelStyle.setLeft(left, Style.Unit.PX);
        panel.add(new HTML("<svg width=\"" + width + "\" height=\"" + height + "\">\n" +
                "<defs>" +
                "    <filter id=\"shadow\" x=\"0\" y=\"0\" width=\"200%\" height=\"200%\">\n" +
                "  <feGaussianBlur in=\"SourceAlpha\" stdDeviation=\"1\"/> <!-- stdDeviation is how much to blur -->\n" +
                "  <feOffset dx=\"1\" dy=\"2\" result=\"offsetblur\"/> <!-- how much to offset -->\n" +
                "  <feMerge> \n" +
                "    <feMergeNode/> <!-- this contains the offset blurred image -->\n" +
                "    <feMergeNode in=\"SourceGraphic\"/> <!-- this contains the element that the filter is applied to -->\n" +
                "  </feMerge>" +
                "    </filter>" +
                "    <marker id=\"markerCircle\" markerWidth=\"2\" markerHeight=\"2\" refX=\"2\" refY=\"2\">" +
                "        <circle cx=\"2\" cy=\"2\" r=\"1\" style=\"stroke: none; fill:" + color + ";\"/>" +
                "    </marker>" +
                "</defs>" +
                "<path d=\"" + path + "\"\n" +
                "      style=\"stroke:" + color + "; stroke-width: 2px; fill: none; " +
/*
                "marker-end: url(#markerCircle);\n" +
*/
                "\"\n " +
/*
                "filter=\"url(#shadow)\" " +
*/
                "/>\n" +
                "    \n" +
                "</svg>"));
        panel.setVisible(true);
    }

    static private native boolean supportsSVG() /*-{
        return typeof SVGRect != "undefined";;
    }-*/;

    public void hide() {
        panel.setVisible(false);
    }

    @Override
    public void onActivityClose() {
        hide();
    }
}
