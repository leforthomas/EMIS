package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.MyCalendarResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.CalendarModel;
import com.google.gwt.user.datepicker.client.CalendarRange;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.WidgetUtil;

import java.util.Date;

/**
 * Created by thomas on 27/02/2015.
 */
public class DateRangePicker extends Composite implements CalendarRange.Presenter {

    public static interface Presenter {

        void onCompleted(Date startDate, Date stopDate);
    }

    private static DateTimeFormat fmtDateOnly = DateTimeFormat.getFormat("MMM dd, yyyy");

    private static enum DATE_RANGES {lastDay, lastWeek, lastMonth, lastYear};

    interface DateRangePickerUiBinder extends UiBinder<Widget, DateRangePicker> {
    }

    private static DateRangePickerUiBinder uiBinder = GWT.create(DateRangePickerUiBinder.class);

    static public interface Style extends CssResource {

        String enabled();

        String focus();

        String day();

        String error();

        String arrowDown();
    }

    @UiField
    Style style;

    @UiField
    MenuArrowedPanel panel;
    @UiField
    TextBox startDate;
    @UiField
    TextBox stopDate;
    @UiField
    Anchor apply;
    @UiField
    Anchor backMonth;
    @UiField
    Anchor forwardMonth;
    @UiField(provided = true)
    CalendarRange firstMonth;
    @UiField(provided = true)
    CalendarRange secondMonth;
    @UiField(provided = true)
    CalendarRange thirdMonth;
    @UiField
    Anchor firstMonthLabel;
    @UiField
    Anchor secondMonthLabel;
    @UiField
    Anchor thirdMonthLabel;
    @UiField
    ListBox dateRanges;

    private Presenter presenter;

    public DateRangePicker() {

        MyCalendarResources.INSTANCE.calendarRangeStyle().ensureInjected();
        CalendarModel firstModel = new CalendarModel();
        firstMonth = new CalendarRange();
        firstMonth.setStyle(MyCalendarResources.INSTANCE.calendarRangeStyle());
        firstMonth.setModel(firstModel);
        firstMonth.setPresenter(this);
        firstMonth.setup();
        CalendarModel secondModel = new CalendarModel();
        secondMonth = new CalendarRange();
        secondMonth.setStyle(MyCalendarResources.INSTANCE.calendarRangeStyle());
        secondMonth.setModel(secondModel);
        secondMonth.setPresenter(this);
        secondMonth.setup();
        CalendarModel thirdModel = new CalendarModel();
        thirdMonth = new CalendarRange();
        thirdMonth.setStyle(MyCalendarResources.INSTANCE.calendarRangeStyle());
        thirdMonth.setModel(thirdModel);
        thirdMonth.setPresenter(this);
        thirdMonth.setup();

        initWidget(uiBinder.createAndBindUi(this));

        Element spanElement = DOM.createSpan();
        DOM.appendChild(panel.getElement(), spanElement);
        spanElement.addClassName(style.arrowDown());

        backMonth.setHTML("&laquo;");
        backMonth.addClickHandler(event -> setStartMonth(DateUtil.addMonths(firstMonth.getModel().getCurrentMonth(), -1)));
        forwardMonth.setHTML("&raquo;");
        forwardMonth.addClickHandler(event -> setStartMonth(DateUtil.addMonths(firstMonth.getModel().getCurrentMonth(), 1)));

        startDate.addFocusHandler(event -> {
            setDateFocus(true);
            updateDateRangeDisplay();
        });
        stopDate.addFocusHandler(event -> {
            setDateFocus(false);
            updateDateRangeDisplay();
        });
        WidgetUtil.setDelayedKeyUpHandler(startDate, nativeKeyCode -> {
            try {
                Date date = getStartDate();
                setStartDate(date);
            } catch (Exception e) {
                startDate.setStyleName(style.error());
            }
        });
        WidgetUtil.setDelayedKeyUpHandler(stopDate, nativeKeyCode -> {
            try {
                Date date = getStopDate();
                setStopDate(date);
            } catch (Exception e) {
                stopDate.setStyleName(style.error());
            }
        });

        dateRanges.addItem("Select", "");
        dateRanges.addItem("Last day", DATE_RANGES.lastDay.toString());
        dateRanges.addItem("Last 7 days", DATE_RANGES.lastWeek.toString());
        dateRanges.addItem("Last Month", DATE_RANGES.lastMonth.toString());
        dateRanges.addItem("Last Year", DATE_RANGES.lastYear.toString());
        dateRanges.addChangeHandler(event -> {
            String selection = dateRanges.getValue(dateRanges.getSelectedIndex());
            if(selection.length() == 0) {
                return;
            }
            Date now = new Date();
            switch(DATE_RANGES.valueOf(selection)) {
                case lastDay:
                    setStartDate(DateUtil.addDays(now, -1));
                    setStopDate(now);
                    break;
                case lastWeek:
                    setStartDate(DateUtil.addDays(now, -7));
                    setStopDate(now);
                    break;
                case lastMonth:
                    setStartDate(DateUtil.addMonths(now, -1));
                    setStopDate(now);
                    break;
                case lastYear:
                    setStartDate(DateUtil.addMonths(now, -12));
                    setStopDate(now);
                    break;
            }
        });

        setDateFocus(true);

        // initialise with the current date
        setStartDate(new Date());
        setStopDate(DateUtil.addDays(new Date(), 7));

    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void setResource(ImageResource resource) {
        panel.setResource(resource);
    }

    public void setLabel(String label) {
        panel.setText(label);
    }

    public void setTooltip(String tooltip) {
        panel.setTooltip(tooltip);
    }

    private void setDateFocus(boolean startFocus) {
        startDate.setFocus(startFocus);
        startDate.setStyleName(style.focus(), startFocus);
        stopDate.setFocus(!startFocus);
        stopDate.setStyleName(style.focus(), !startFocus);
        // shift date ranges to have the start or stop date visible

    }

    public void setStartMonth(Date date) {
        // move the models to have the appropriate current months
        firstMonth.getModel().setCurrentMonth(date);
        secondMonth.getModel().setCurrentMonth(DateUtil.addMonths(date, 1));
        thirdMonth.getModel().setCurrentMonth(DateUtil.addMonths(date, 2));
        refreshCalendars();
        firstMonthLabel.setText(firstMonth.getModel().formatCurrentMonthAndYear());
        secondMonthLabel.setText(secondMonth.getModel().formatCurrentMonthAndYear());
        thirdMonthLabel.setText(thirdMonth.getModel().formatCurrentMonthAndYear());
        updateDateRangeDisplay();
    }

    private void refreshCalendars() {
        firstMonth.refresh();
        secondMonth.refresh();
        thirdMonth.refresh();
    }

    public void setStartDate(Date date) {
        startDate.setStyleName(style.error(), false);
        startDate.setText(formatDate(date));
        if(!dateInRange(date)) {
            setStartMonth(DateUtil.addMonths(date, -2));
        }
        updateDateRangeDisplay();
    }

    private Date getStartDate() throws IllegalArgumentException {
        return parseDate(startDate.getText());
    }

    public void setStopDate(Date date) {
        stopDate.setText(formatDate(date));
        if(!dateInRange(date)) {
            setStartMonth(DateUtil.addMonths(date, -2));
        }
        updateDateRangeDisplay();
    }

    private Date getStopDate() throws IllegalArgumentException {
        return parseDate(stopDate.getText());
    }

    public void setMinDate(Date date) {
        firstMonth.setMinDate(date);
        secondMonth.setMinDate(date);
        thirdMonth.setMinDate(date);
    }

    public void setMaxDate(Date date) {
        date.setHours(23);
        date.setMinutes(59);
        date.setSeconds(59);
        firstMonth.setMaxDate(date);
        secondMonth.setMaxDate(date);
        thirdMonth.setMaxDate(date);
    }

    public String formatDate(Date date) {
        return fmtDateOnly.format(date);
    }

    private Date parseDate(String stringDate) throws IllegalArgumentException {
        Date date = fmtDateOnly.parseStrict(stringDate);
        return date;
    }

    private boolean dateInRange(Date date) {
        int month = date.getMonth();
        int year = date.getYear();
        return (firstMonth.getModel().getCurrentMonth().getMonth() == month &&  firstMonth.getModel().getCurrentMonth().getYear() == year) ||
                (secondMonth.getModel().getCurrentMonth().getMonth() == month &&  secondMonth.getModel().getCurrentMonth().getYear() == year) ||
                (thirdMonth.getModel().getCurrentMonth().getMonth() == month &&  thirdMonth.getModel().getCurrentMonth().getYear() == year);
    }

    private void updateDateRangeDisplay() {
        try {
            Date startDate = getStartDate();
            Date stopDate = getStopDate();
            boolean startFocus = isStartFocus();
            firstMonth.setSelectionRange(startDate, stopDate, startFocus);
            secondMonth.setSelectionRange(startDate, stopDate, startFocus);
            thirdMonth.setSelectionRange(startDate, stopDate, startFocus);
        } catch (Exception e) {

        }
    }

    private void enableApplyButton(boolean enable) {
        apply.setEnabled(enable);
        apply.setStyleName(style.enabled());
    }

    @Override
    public void setDateSelected(Date date, boolean b) {
        // check which has the focus
        if(isStartFocus()) {
            setStartDate(date);
            setDateFocus(false);
            try {
                if (getStopDate().before(date)) {
                    setStopDate(DateUtil.addDays(date, 7));
                }
            } catch (Exception e) {

            }
        } else {
            setStopDate(date);
            setDateFocus(true);
            try {
                if (getStartDate().after(date)) {
                    setStartDate(DateUtil.addDays(date, -7));
                }
            } catch (Exception e) {

            }
        }
    }

    private boolean isStartFocus() {
        return startDate.getStyleName().contains(style.focus());
    }

    @Override
    public String getStyleOfDate(Date current) {
        return style.day();
    }

    @Override
    public void onHighlightedDate(Date value) {

    }

    @UiHandler("apply")
    void apply(ClickEvent clickEvent) {
        if(presenter != null) {
            presenter.onCompleted(getStartDate(), getStopDate());
        }
        panel.displayMenu(false);
        // make sure date focus is set back to start
        setDateFocus(true);
    }

    @UiHandler("cancel")
    void cancel(ClickEvent clickEvent) {
        panel.displayMenu(false);
        setDateFocus(true);
    }

    public void setMaxTextWidth(String maxWidth) {
        panel.setMaxWidth(maxWidth);
    }

}