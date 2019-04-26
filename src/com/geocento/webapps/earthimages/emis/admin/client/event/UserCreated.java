package com.geocento.webapps.earthimages.emis.admin.client.event;

import com.geocento.webapps.earthimages.emis.common.share.entities.DOMAIN;
import com.geocento.webapps.earthimages.emis.common.share.entities.USAGE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_STATUS;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by thomas on 21/04/2015.
 */
public class UserCreated extends GwtEvent<UserCreatedHandler> {

    public static Type<UserCreatedHandler> TYPE = new Type<UserCreatedHandler>();

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String organisation;
    private USER_ROLE userRole;
    private USER_STATUS userStatus;
    private String countryCode;
    private DOMAIN domain;
    private USAGE usage;
    private String currency;

    public UserCreated(String username, String email, String password, String firstName, String lastName, String organisation, String countryCode, DOMAIN domain, USAGE usage, USER_ROLE userRole, USER_STATUS userStatus, String currency) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.organisation = organisation;
        this.countryCode = countryCode;
        this.domain = domain;
        this.usage = usage;
        this.userRole = userRole;
        this.userStatus = userStatus;
        this.currency = currency;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getOrganisation() {
        return organisation;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public DOMAIN getDomain() {
        return domain;
    }

    public USAGE getUsage() {
        return usage;
    }

    public USER_ROLE getUserRole() {
        return userRole;
    }

    public USER_STATUS getUserStatus() {
        return userStatus;
    }

    public Type<UserCreatedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(UserCreatedHandler handler) {
        handler.onUserCreated(this);
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
