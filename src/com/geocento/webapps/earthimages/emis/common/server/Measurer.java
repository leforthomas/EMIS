package com.geocento.webapps.earthimages.emis.common.server;

import org.apache.log4j.Logger;

public class Measurer {

	private long currentTime;
	
	private Logger logger;
	
	public Measurer() {
		this(null);
	}
	
	public Measurer(Logger logger) {
		this.logger = logger;
	}

	public void startMeasuring() {
		currentTime = System.currentTimeMillis();
	}
	
	public void stopMeasuring(String message) {
		if(this.logger != null) {
			logger.info(message + " " + (System.currentTimeMillis() - currentTime) + "ms");
		}
	}
	
	public long stopMeasuring() {
		return (System.currentTimeMillis() - currentTime);
	}
	
}
