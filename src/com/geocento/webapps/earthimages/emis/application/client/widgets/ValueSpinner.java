package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.Spinner;
import com.metaaps.webapps.libraries.client.widget.SpinnerListener;
import com.metaaps.webapps.libraries.client.widget.util.ValueChangeHandler;

/**
 * Created by thomas on 06/02/2017.
 */
public class ValueSpinner extends Composite {

    interface ValueSpinnerUiBinder extends UiBinder<HTMLPanel, ValueSpinner> {
    }

    private static ValueSpinnerUiBinder ourUiBinder = GWT.create(ValueSpinnerUiBinder.class);

    @UiField
    VerticalPanel spinnerPanel;
    @UiField
    SpanElement text;
    @UiField
    TextBox textBox;

    private static final String STYLENAME_DEFAULT = "gwt-ValueSpinner";

    private Spinner spinner;

    private ValueChangeHandler<Long> changeHandler;

    private SpinnerListener spinnerListener = new SpinnerListener() {

        private Timer timer;

        public void onSpinning(final long value) {
            if (getSpinner() != null) {
                getSpinner().setValue(value, false);
            }
            textBox.setText(formatValue(value));
            if(changeHandler != null) {
                if(timer != null) {
                    timer.cancel();
                    timer = null;
                }
                timer = new Timer() {

                    @Override
                    public void run() {
                        changeHandler.onValueChanged(value);
                        timer = null;
                    }
                };
                timer.schedule(500);
            }
        }

    };

    private KeyPressHandler keyPressHandler = new KeyPressHandler() {

        public void onKeyPress(KeyPressEvent event) {
            int index = textBox.getCursorPos();
            String previousText = textBox.getText();
            String newText;
            if (textBox.getSelectionLength() > 0) {
                newText = previousText.substring(0, textBox.getCursorPos())
                        + event.getCharCode()
                        + previousText.substring(textBox.getCursorPos()
                        + textBox.getSelectionLength(), previousText.length());
            } else {
                newText = previousText.substring(0, index) + event.getCharCode()
                        + previousText.substring(index, previousText.length());
            }
            textBox.cancelKey();
            try {
                long newValue = parseValue(newText);
                if (spinner.isConstrained()
                        && (newValue > spinner.getMax() || newValue < spinner.getMin())) {
                    return;
                }
                spinner.setValue(newValue, true);
            } catch (Exception e) {
                // textBox.cancelKey();
            }
        }
    };

    private NumberFormat formatter = NumberFormat.getDecimalFormat();

    public ValueSpinner() {
        this(0, 0, 100);
    }

    public ValueSpinner(long value, int min, int max) {
        this(value, min, max, 1, 10);
    }

    public ValueSpinner(long value, int min, int max, int minStep, int maxStep) {

        initWidget(ourUiBinder.createAndBindUi(this));

        // set the key press handler
        textBox.addKeyPressHandler(keyPressHandler);

        // set default values
        spinner = new Spinner(spinnerListener, value, min, max, minStep, maxStep, true);

        // add arrows to spinner panel
        spinnerPanel.add(spinner.getIncrementArrow());
        spinnerPanel.setCellVerticalAlignment(spinner.getIncrementArrow(), HasVerticalAlignment.ALIGN_BOTTOM);
        spinnerPanel.add(spinner.getDecrementArrow());
        spinnerPanel.setCellVerticalAlignment(spinner.getDecrementArrow(), HasVerticalAlignment.ALIGN_TOP);

    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setSpinner(Spinner spinner) {
        this.spinner = spinner;
    }

    public SpinnerListener getSpinnerListener() {
        return spinnerListener;
    }

    public void setSpinnerListener(SpinnerListener spinnerListener) {
        this.spinnerListener = spinnerListener;
    }

    public void setChangeHandler(ValueChangeHandler<Long> changeHandler) {
        this.changeHandler = changeHandler;
    }

    /**
     * @return the TextBox used by this widget
     */
    public TextBox getTextBox() {
        return textBox;
    }

    public void setValue(long value) {
        spinner.setValue(value, false);
        textBox.setValue(formatValue(value));
    }

    /**
     * call to change the formatting pattern
     * @param pattern
     */
    public void setFormatterPattern(String pattern) {
        formatter = NumberFormat.getFormat(pattern);
    }

    public void setFormatter(NumberFormat formatter) {
        this.formatter = formatter;
    }

    /**
     * @return whether this widget is enabled.
     */
    public boolean isEnabled() {
        return spinner.isEnabled();
    }

    /**
     * Sets whether this widget is enabled.
     *
     * @param enabled true to enable the widget, false to disable it
     */
    public void setEnabled(boolean enabled) {
        spinner.setEnabled(enabled);
        textBox.setEnabled(enabled);
    }

    /**
     * @param value the value to format
     * @return the formatted value
     */
    protected String formatValue(long value) {
        return formatter.format(value);
    }

    /**
     * @param value the value to parse
     * @return the parsed value
     */
    protected long parseValue(String value) {
        return Long.valueOf(value);
    }

    public long getValue() {
        return parseValue(textBox.getValue());
    }

    public void setText(String text) {
        this.text.setInnerText(text);
    }

    public void setMinValue(long value) {
        spinner.setMin(value);
    }

    public void setMaxValue(long value) {
        spinner.setMax(value);
    }

}