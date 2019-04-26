package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.metaaps.webapps.earthimages.extapi.server.domain.Product;
import com.metaaps.webapps.earthimages.extapi.server.domain.TYPE;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;

import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 16/02/2015.
 */
public class TimeGrid extends Composite {

    interface TimeGridUiBinder extends UiBinder<HTMLPanel, TimeGrid> {
    }

    private static TimeGridUiBinder ourUiBinder = GWT.create(TimeGridUiBinder.class);

    public interface Style extends CssResource {

        String timeLabel();

        String timeTick();

        String timeTickLarge();

        String productTick();

        String productArchiveTick();

        String productFutureTick();

        String originalTimeFrame();
    }

    @UiField Style style;

    @UiField
    AbsolutePanel timeGrid;

    private Date startDate;
    private Date stopDate;

    public TimeGrid() {

        initWidget(ourUiBinder.createAndBindUi(this));

    }

    public void setTimeFrame(Date startDate, Date stopDate) {
        this.startDate = startDate;
        this.stopDate = stopDate;
        timeGrid.clear();
        drawBands();
    }

    public void setProducts(List<Product> products) {
        // clear the timeGrid
        timeGrid.clear();
        drawBands();
        // add the ticks
        double width = timeGrid.getOffsetWidth();
        for (Product product : products) {
            HTMLPanel tickPanel = new HTMLPanel("");
            tickPanel.addStyleName(style.productTick());
            tickPanel.addStyleName(product.getType() == TYPE.ARCHIVE ? style.productArchiveTick() : style.productFutureTick());
            timeGrid.add(tickPanel, getDatePosition(product.getStart()), 5);
        }
    }

    private void drawBands() {
        Date nowTime = new Date();
        // draw bands based on duration with minimum 3 bands
        DateUtil.BANDTYPE bandType = DateUtil.getTimeBand(startDate, stopDate, 3);
        // find the position of the next band
        Date currentDate;
        // start with 150 to make sure the first label is added
        int maxDistance = 150;
        switch(bandType) {
            case YEAR:
                currentDate = DateUtil.truncateToYear(startDate);
                maxDistance = 100;
                break;
            case MONTH:
                currentDate = DateUtil.truncateToMonth(startDate);
                maxDistance = 100;
                break;
            case DAY:
                currentDate = DateUtil.truncateToDay(startDate);
                maxDistance = 100;
                break;
            case HOUR:
                currentDate = DateUtil.truncateToHour(startDate);
                maxDistance = 120;
                break;
            case MN:
                currentDate = DateUtil.truncateToMinutes(startDate);
                maxDistance = 120;
                break;
            case SEC:
                currentDate = DateUtil.truncateToSeconds(startDate);
                maxDistance = 120;
                break;
            default:
                currentDate = startDate;
                maxDistance = 150;
        }
        int numBands = 0;
        boolean first = true;
        int distance = 0;
        // update the date values
        for(; currentDate.before(stopDate); numBands++) {
            int position = getDatePosition(currentDate);
            if(position > 0) {
                // add a tick for each iteration
                HTMLPanel tickPanel = new HTMLPanel("");
                tickPanel.addStyleName(style.timeTick());
                timeGrid.add(tickPanel, position, 0);
                // check if we need to add a label
                // try to place a label every maxDistance pixels
                boolean addLabel = first || distance > maxDistance;
                if (addLabel) {
                    String labelString = "";
                    switch (bandType) {
                        case YEAR:
                            labelString = DateUtil.displayDateOnly(currentDate);
                            break;
                        case MONTH:
                            labelString = DateUtil.displayDateOnly(currentDate);
                            break;
                        case DAY:
                            labelString = DateUtil.displayDateOnly(currentDate);
                            break;
                        default:
                            labelString = DateUtil.displayTimeOnly(currentDate);
                            if (first) {
                                labelString = DateUtil.displaySimpleUTCDate(currentDate);
                            }
                    }
                    int datePosition = (int) getDatePosition(currentDate);
                    // add a label
                    HTMLPanel timeLabel = new HTMLPanel(labelString);
                    timeLabel.addStyleName(style.timeLabel());
                    timeGrid.add(timeLabel, datePosition, 20);
                    // make tick larger
                    tickPanel.addStyleName(style.timeTickLarge());
                    // reset if labels have been added
                    distance = 0;
                    first = false;
                }
            }
            // increment the current date
            switch (bandType) {
                case YEAR:
                    currentDate.setYear(currentDate.getYear() + 1);
                    break;
                case MONTH:
                    currentDate.setMonth(currentDate.getMonth() + 1);
                    break;
                case DAY:
                    currentDate = new Date(currentDate.getTime() + DateUtil.dayInMs);
                    break;
                case HOUR:
                    currentDate = new Date(currentDate.getTime() + DateUtil.hourInMs);
                    break;
                case MN:
                    currentDate = new Date(currentDate.getTime() + DateUtil.minuteInMs);
                    break;
                case SEC:
                    currentDate = new Date(currentDate.getTime() + DateUtil.secondInMs);
                    break;
                default:
                    currentDate = new Date(currentDate.getTime() + 10);
            }
            int stopPos = getDatePosition(currentDate);
            // add the width
            distance += stopPos - position;
        }
    }

    private int getDatePosition(Date date) {
        double duration = stopDate.getTime() - startDate.getTime();
        double width = timeGrid.getOffsetWidth();
        return (int) (((double) date.getTime() - startDate.getTime()) / duration * width);
    }

    public void clearAll() {
        timeGrid.clear();
    }

}