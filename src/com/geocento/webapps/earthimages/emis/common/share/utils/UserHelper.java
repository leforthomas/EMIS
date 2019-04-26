package com.geocento.webapps.earthimages.emis.common.share.utils;

import com.geocento.webapps.earthimages.emis.common.share.EIException;

/**
 * Created by thomas on 30/01/2017.
 */
public class UserHelper {

    public static String usernameRegexp = "^([A-Za-z]|[0-9]|_)+$";

    public static String emailregExp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\."
            +"[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            +"(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

    public static void checkEmail(String emailAddressString) throws EIException {
        if(emailAddressString.length() < 5 || !emailAddressString.toLowerCase().matches(UserHelper.emailregExp)) {
            throw new EIException("Email address is not valid");
        }
    }

    public static void checkUserName(String username) throws EIException {
        if(username.length() < 5) {
            throw new EIException("User name too short - min 5 characters");
        }
        if(!username.matches(UserHelper.usernameRegexp)) {
            throw new EIException("User name can only contain alphanumeric chars");
        }
    }

    public static void checkPasswordValidity(String userPassword) throws EIException {
        if(userPassword.length() < 8) {
            throw new EIException("Password too short - min 8 characters");
        }
    }

    public static void checkFirstName(String firstName) throws EIException {
        if(firstName.length() == 0) {
            throw new EIException("Please provide a first name");
        }
    }

    public static void checkLastName(String lastName) throws EIException {
        if(lastName.length() == 0) {
            throw new EIException("Please provide a last name");
        }
    }

    public static void checkOrganisation(String organisation) throws EIException {
        if(organisation.length() == 0) {
            throw new EIException("Please provide an organisation, eg company name");
        }
    }

}
