package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.ArrowedPopup;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.SliderMovedHandler;
import com.metaaps.webapps.libraries.client.widget.util.Sliding;
import com.metaaps.webapps.libraries.client.widget.util.Util.TYPE;

import java.util.*;

public class TimeWidget extends Composite {

	private static TimeWidgetUiBinder uiBinder = GWT
			.create(TimeWidgetUiBinder.class);

	interface TimeWidgetUiBinder extends UiBinder<Widget, TimeWidget> {
	}
	
	static public interface Presenter {
		void onTimeChanged(Date currentTime, Date startTimeFrame, Date stopTimeFrame);
	}
	
	static private StyleResources styleResources = GWT.create(StyleResources.class);
	
	interface Style extends CssResource {
	}
	
	@UiField Style style;
	
	@UiField AbsolutePanel sliderBarArea;
	@UiField AbsolutePanel stepsDates;
	@UiField Image firstSlider;
	@UiField Image secondSlider;
	@UiField Image slider;
	
	private int marginLeft = 5;
	private int sliderWidth = 5;
	private int sliderBarWidth = 190;
	private Date startDate = new Date();
	private Date stopDate = new Date();
	private Date currentDate = new Date();
	private Presenter presenter;

	private Date startTimeFrame;
	private Date stopTimeFrame;

	private boolean chained;

	private List<Date> stepsDatesList = new ArrayList<Date>();

	protected ArrowedPopup popup;

	public TimeWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		
		popup = ArrowedPopup.getInstance();
		
		// add a decorator to enable sliding
		new Sliding(slider, sliderBarArea, new SliderMovedHandler() {
			
			@Override
			public void onSliderMoved(int position) {
				setCurrentTime(getPositionDate(position - sliderWidth));
				notifyChanges();
			}

		});
		
		slider.addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent arg0) {
				showTime();
			}
		});
		slider.addMouseMoveHandler(new MouseMoveHandler() {
			
			@Override
			public void onMouseMove(MouseMoveEvent arg0) {
				showTime();
			}
		});
		slider.addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent arg0) {
				popup.hide();
			}
		});

		showRestrictedTimeFrameSliders(false);
		
	}
	
	private void showTime() {
		popup.showAt(slider, "<p style=\"font-size: 0.9em; margin: 0px; padding: 0px;\">Current time " + DateUtil.displaySimpleUTCDate(currentDate) + "</p>", TYPE.below);
	}
	
	private void notifyChanges() {
		if(presenter != null) {
			presenter.onTimeChanged(currentDate, isRestrictedTimeFrameEnabled() ? startTimeFrame : startDate, isRestrictedTimeFrameEnabled() ? stopTimeFrame : stopDate);
		}
	}

	public void showTimeSlider(boolean show) {
		slider.setVisible(show);
		sliderBarArea.setWidgetPosition(slider, -100, -100);
	}
	
	public void showRestrictedTimeFrameSliders(boolean show) {
		firstSlider.setVisible(show);
		secondSlider.setVisible(show);
	}

	private boolean isRestrictedTimeFrameEnabled() {
		return firstSlider.isVisible();
	}

	public void showRestrictedTimeFrame(Date startTimeFrame, Date stopTimeFrame) {
		// show and intialise the two time frame sliders
		showRestrictedTimeFrameSliders(true);
		new Sliding(firstSlider, sliderBarArea, new SliderMovedHandler() {
			
			@Override
			public void onSliderMoved(int position) {
				// assumes the mouse position returned corresponds to the middle of the slider
				setRestrictedStartTimeFrame(getPositionDate(position));
				updateSlidersPosition();
				notifyChanges();
			}
		});
		new Sliding(secondSlider, sliderBarArea, new SliderMovedHandler() {
			
			@Override
			public void onSliderMoved(int position) {
				setRestrictedStopTimeFrame(getPositionDate(position));
				updateSlidersPosition();
				notifyChanges();
			}
		});
		// set values for the time frame
		setRestrictedStartTimeFrame(startTimeFrame == null ? startDate : startTimeFrame);
		setRestrictedStopTimeFrame(stopTimeFrame == null ? stopDate : stopTimeFrame);
		updateSlidersPosition();
	}
	
	private void setRestrictedStartTimeFrame(Date startTimeFrame) {
		if(slider.isVisible()) {
			if(chained) {
				// calculate the difference
				long timeDifference = startTimeFrame.getTime() - this.startTimeFrame.getTime();
				shiftRestrictedTimeFrame(timeDifference);
			} else {
				this.startTimeFrame = startTimeFrame.before(startDate) ? startDate : startTimeFrame.after(currentDate) ? currentDate : startTimeFrame;
			}
		} else {
			this.startTimeFrame = startTimeFrame.before(startDate) ? startDate : startTimeFrame;
		}
	}
	
	private void setRestrictedStopTimeFrame(Date stopTimeFrame) {
		if(slider.isVisible()) {
			if(chained) {
				// calculate the difference
				long timeDifference = stopTimeFrame.getTime() - this.stopTimeFrame.getTime();
				shiftRestrictedTimeFrame(timeDifference);
			} else {
				this.stopTimeFrame = stopTimeFrame.after(stopDate) ? stopDate : stopTimeFrame.before(currentDate) ? currentDate : stopTimeFrame;
			}
		} else {
			this.stopTimeFrame = stopTimeFrame.after(stopDate) ? stopDate : stopTimeFrame;
		}
	}
	
	private void shiftRestrictedTimeFrame(long timeDifference) {
		// start by moving the start time frame
		Date startTimeFrame = new Date(this.startTimeFrame.getTime() + timeDifference);
		// update the date based on constraints
		startTimeFrame = startTimeFrame.before(startDate) ? startDate : startTimeFrame.after(currentDate) ? currentDate : startTimeFrame;
		// calculate the start time difference
		long startTimeDifference = startTimeFrame.getTime() - this.startTimeFrame.getTime();
		// now move the stop time frame
		Date stopTimeFrame = new Date(this.stopTimeFrame.getTime() + timeDifference);
		// update the date based on constraints
		stopTimeFrame = stopTimeFrame.after(stopDate) ? stopDate : stopTimeFrame.before(currentDate) ? currentDate : stopTimeFrame;
		// calculate the stop time difference
		long stopTimeDifference = stopTimeFrame.getTime() - this.stopTimeFrame.getTime();
		// update the timeDifference to be the lowest of the two
		if(Math.abs(startTimeDifference) < Math.abs(stopTimeDifference)) {
			timeDifference = startTimeDifference;
		} else {
			timeDifference = stopTimeDifference;
		}
		// update the restricted time frame
		this.startTimeFrame = new Date(this.startTimeFrame.getTime() + timeDifference);
		this.stopTimeFrame = new Date(this.stopTimeFrame.getTime() + timeDifference);
	}
	
	/*
	 * sets the min and max time values of the slider
	 */
	public void setTimeFrame(Date startDate, Date stopDate) {
		if(startDate == null || stopDate == null || startDate.after(stopDate)) {
			return;
		}
		this.startDate = startDate;
		this.stopDate = stopDate;
		this.startTimeFrame = startDate;
		this.stopTimeFrame = stopDate;
		checkCurrentTime();
		// refresh the display
		updateSlidersPosition();
	}
	
	private void checkCurrentTime() {
		// check value of current date is within the time frame
		if(startDate.after(currentDate)) {
			currentDate = startDate;
		} else if(stopDate.before(currentDate)) {
			currentDate = stopDate;
		}
		// move time frame sliders if necessary
		if(isRestrictedTimeFrameEnabled()) {
			if(startTimeFrame.after(currentDate)) {
				setRestrictedStartTimeFrame(currentDate);
			}
			if(stopTimeFrame.before(currentDate)) {
				setRestrictedStopTimeFrame(currentDate);
			}
		}
	}

	public void setCurrentTime(Date currentDate) {
		setCurrentTime(currentDate, false);
	}

	public void setCurrentTime(Date currentDate, boolean notify) {
		this.currentDate = currentDate;
		checkCurrentTime();
		// refresh the display
		updateSlidersPosition();
		if(notify) {
			notifyChanges();
		}
	}

	private Date getPositionDate(int position) {
		long startTime = startDate.getTime();
		long dateValue = startTime + (long) ((position - marginLeft) * (((double) stopDate.getTime() - startTime) / (double) sliderBarWidth));
		return new Date(dateValue);
	}
	
	private int getDatePosition(Date date) {
		long startTime = startDate.getTime();
		return (int) ((((double) date.getTime() - startTime) / ((double) stopDate.getTime() - startTime)) * sliderBarWidth) + marginLeft;
	}

	private void updateSlidersPosition() {
		// update the time slider if available
		if(slider.isVisible()) {
			// calculate the position of the slider
			int position = getDatePosition(currentDate);
			sliderBarArea.setWidgetPosition(slider, position - sliderWidth, 4);
		}
		// update the time frame sliders if available
		if(firstSlider.isVisible()) {
			// update the start time slider position
			sliderBarArea.setWidgetPosition(firstSlider, getDatePosition(startTimeFrame) - sliderWidth, 4);
			sliderBarArea.setWidgetPosition(secondSlider, getDatePosition(stopTimeFrame) - sliderWidth, 4);
		}
	}

	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	public Date getCurrentTime() {
		return currentDate;
	}

	public void setChained(boolean chained) {
		this.chained = chained;
	}
	
	public void setMinimized(boolean minimized) {
		if(minimized) {
			setRestrictedStartTimeFrame(currentDate);
			setRestrictedStopTimeFrame(currentDate);
		} else {
			setRestrictedStartTimeFrame(startDate);
			setRestrictedStopTimeFrame(stopDate);
		}
		setChained(minimized);
		updateSlidersPosition();
	}
	
	// add markers along the slider to highlight the different steps available
	public void setSteps(List<Date> stepsDatesList) {
		stepsDates.clear();
		this.stepsDatesList.clear();
		if(stepsDatesList == null) {
			return;
		}
		// first order the list by date
		this.stepsDatesList.addAll(stepsDatesList);
		Collections.sort(this.stepsDatesList, new Comparator<Date>() {

			@Override
			public int compare(Date o1, Date o2) {
				return o1.compareTo(o2);
			}
		});
		for(Date value : this.stepsDatesList) {
			Label tick = new Label("");
			tick.getElement().getStyle().setProperty("borderLeft", "1px solid #888");
			tick.setHeight("100%");
			stepsDates.add(tick, getDatePosition(value), 0);
		}
	}
	
	public Date getStartTimeFrame() {
		return startTimeFrame;
	}
	
	public Date getStopTimeFrame() {
		return stopTimeFrame;
	}
	
}
