package com.geocento.webapps.earthimages.emis.common.share.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.google.gwt.regexp.shared.RegExp;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.util.List;

/**
 * Created by thomas on 19/10/2015.
 */
public class CreditCardHelper {

    static private List<RegExp> creditCardNumberCheckers =
            ListUtil.toList(new RegExp[]{
                    RegExp.compile("^(?:4[0-9]{12}(?:[0-9]{3})?)$"),    // Visa
                    RegExp.compile("^(5[1-5][0-9]{14})$"),  // MasterCard
                    RegExp.compile("^(3[47][0-9]{13})$"), // American Express
                    RegExp.compile("^(3(?:0[0-5]|[68][0-9])[0-9]{11})$"),   // Diners Club
                    RegExp.compile("^(6(?:011|5[0-9]{2})[0-9]{12})$"),  // Discover
                    RegExp.compile("^((?:2131|1800|35\\d{3})\\d{11})$")    // JCB
            });

    public static RegExp cvvChecker = RegExp.compile("^([0-9]{3,4})$");

    public static RegExp monthChecker = RegExp.compile("^([0-1]{1}[0-9]{1})$");

    public static RegExp yearChecker = RegExp.compile("^(20[0-2]{1}[0-9]{1})$");

    public static void checkNumber(String number) throws EIException {
        boolean creditCardValid = false;
        for(RegExp regExp : creditCardNumberCheckers) {
            creditCardValid = creditCardValid || regExp.test(number);
        }
        if (!creditCardValid) {
            throw new EIException("Card number is not valid");
        }
    }

    public static void checkMonth(String month) throws EIException {
        if(!monthChecker.test(month)) {
            throw new EIException("Month is not valid");
        }
    }

    public static void checkYear(String year) throws EIException {
        if(!yearChecker.test(year)) {
            throw new EIException("Year is not valid");
        }
    }

    public static void checkCVV(String cvv) throws EIException {
        if(!cvvChecker.test(cvv)) {
            throw new EIException("CVV is not valid");
        }
    }
}
