package com.geocento.webapps.earthimages.emis.admin.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.DOMAIN;
import com.geocento.webapps.earthimages.emis.common.share.entities.USAGE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_STATUS;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by thomas on 9/01/15.
 */
public class UserDTO implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    String username;

    String firstName;

    String lastName;

    USER_ROLE userRole;

    USER_STATUS userStatus;

    // 2 letters country codes as per ISO 3166-1 alpha-2 codes
    String countryCode;

    String email;

    String company;

    DOMAIN domain;

    USAGE usage;

    CreditDTO credit;

    boolean canOrder;

    Date registeredDate;

    Date lastLoggedIn;

    String address;
    String phone;

    boolean chargeVAT;
    private boolean needsVATNumber;
    private String communityVATNumber;
    private String password;

    public UserDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public USER_ROLE getUserRole() {
        return userRole;
    }

    public void setUserRole(USER_ROLE userRole) {
        this.userRole = userRole;
    }

    public USER_STATUS getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(USER_STATUS userStatus) {
        this.userStatus = userStatus;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public DOMAIN getDomain() {
        return domain;
    }

    public void setDomain(DOMAIN domain) {
        this.domain = domain;
    }

    public USAGE getUsage() {
        return usage;
    }

    public void setUsage(USAGE usage) {
        this.usage = usage;
    }

    public CreditDTO getCredit() {
        return credit;
    }

    public void setCredit(CreditDTO credit) {
        this.credit = credit;
    }

    public boolean isCanOrder() {
        return canOrder;
    }

    public void setCanOrder(boolean canOrder) {
        this.canOrder = canOrder;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(Date registeredDate) {
        this.registeredDate = registeredDate;
    }

    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setChargeVAT(boolean chargeVAT) {
        this.chargeVAT = chargeVAT;
    }

    public boolean isChargeVAT() {
        return chargeVAT;
    }

    public void setNeedsVATNumber(boolean needsVATNumber) {
        this.needsVATNumber = needsVATNumber;
    }

    public boolean isNeedsVATNumber() {
        return needsVATNumber;
    }

    public void setCommunityVATNumber(String communityVATNumber) {
        this.communityVATNumber = communityVATNumber;
    }

    public String getCommunityVATNumber() {
        return communityVATNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
