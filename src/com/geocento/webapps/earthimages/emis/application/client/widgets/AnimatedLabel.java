package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;

import java.util.List;

/**
 * Created by thomas on 08/10/2014.
 */
public class AnimatedLabel extends Label {

    private Timer timer;

    public AnimatedLabel() {
    }

    public void animate(final List<String> labels, int delayMs) {
        timer = new Timer() {

            int index = 0;

            @Override
            public void run() {
                setText(labels.get(index));
                index = (index + 1) % labels.size();
            }
        };
        timer.scheduleRepeating(delayMs);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        stop();
    }

    public void stop() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
