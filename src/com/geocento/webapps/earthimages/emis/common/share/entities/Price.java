package com.geocento.webapps.earthimages.emis.common.share.entities;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.google.gwt.i18n.client.NumberFormat;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
public class Price implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static String[] supportedCurrencies = new String[] {"EUR", "GBP", "USD", "CAD", "USD", "CNY", "INR"};

	@Basic
	private Double value;
	
	// the three letters currency code as per ISO 4217 Currency Codes
	@Basic
	@Size(min=3, max=3)
	@Column(length=3)
	private String currency;

	public Price() {
		this(0.0, null);
	}
	
	public Price(double value, String currency) {
		this.value = value;
		this.currency = currency;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Double getValue() {
		return value;
	}

//	public void changeCurrency(String currency) throws EIException {
//		value = currencyRates.convert(currency, this.currency, value);
//		this.currency = currency;
//	}

	public String getCurrency() {
		return currency;
	}
	
	@Override
	public String toString() {
		if(value != null && currency != null) {
            return NumberFormat.getCurrencyFormat(currency).format(value);
		} else {
			return "Unknown";
		}
	}

    public void add(Price price) throws EIException {
        if(price == null || price.getCurrency() == null) {
            return;
        }
		// if no currency defined, use the one from the price to add
		if(currency == null) {
			currency = price.getCurrency();
		}
		// check currencies are the same
		if(currency.contentEquals(price.getCurrency())) {
			value = value + price.getValue();
		} else {
			throw new EIException("Prices use different currencies, requires conversion first");
		}
	}

}