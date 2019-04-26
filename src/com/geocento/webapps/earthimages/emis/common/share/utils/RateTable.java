package com.geocento.webapps.earthimages.emis.common.share.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class RateTable implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String baseRate = "EUR";
	
	// to be specified by a service
	private HashMap<String, Double> currencyRates = new HashMap<String, Double>();
	
	public RateTable() {
	}

	public RateTable(String baseRate, HashMap<String, Double> currencyRates) {
		super();
		this.baseRate = baseRate;
		this.currencyRates = currencyRates;
	}
	
	public void setBaseRate(String baseRate) {
		this.baseRate = baseRate;
	}
	
	public void setCurrencyRate(String currency, Double rateValue) {
		currencyRates.put(currency, rateValue);
	}

	public double convert(String oldCurrency, String newCurrency, double value) throws EIException {
		if(currencyRates != null && currencyRates.containsKey(oldCurrency) && currencyRates.containsKey(newCurrency)) {
			double rate = currencyRates.get(oldCurrency) / currencyRates.get(newCurrency);
			return value / rate;
		}
		throw new EIException("Rates are not available for the specified currencies.");
	}

	public boolean supports(String name) {
		return currencyRates.containsKey(name);
	}

    public Price getConvertedPrice(Price price, String preferredCurrency) throws EIException {
        return price == null ? null : new Price(convert(price.getCurrency(), preferredCurrency, price.getValue()), preferredCurrency);
    }

    public Set<String> getSupportedCurrencies() {
        return currencyRates.keySet();
    }
}
