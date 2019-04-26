package com.geocento.webapps.earthimages.emis.common.share;

import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.utils.RateTable;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_STATUS;

import java.io.Serializable;
import java.util.Date;

// DTO for passing application wide information at startup
public class LoginInfo implements Serializable {

    private boolean loggedIn = false;
    private Date lastLoggedIn;
    private String sessionId;
    private String userName;
    private USER_STATUS userStatus;
    private USER_ROLE userRole;
    private double prepaidValue;
    private String prepaidCurrency;
    private RateTable rateTable;
    private boolean canOrder;
    private boolean chargeVAT;
    private Price minAmount;

    public LoginInfo() {
    }

    public boolean isLoggedIn() {
    return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
    this.loggedIn = loggedIn;
    }

    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }

    public String getSessionId() {
        // TODO Auto-generated method stub
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public USER_STATUS getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(USER_STATUS userStatus) {
        this.userStatus = userStatus;
    }

    public USER_ROLE getUserRole() {
        return userRole;
    }

    public void setUserRole(USER_ROLE userRole) {
        this.userRole = userRole;
    }

    public double getPrepaidValue() {
        return prepaidValue;
    }

    public void setPrepaidValue(double prepaidValue) {
        this.prepaidValue = prepaidValue;
    }

    public String getPrepaidCurrency() {
        return prepaidCurrency;
    }

    public void setPrepaidCurrency(String prepaidCurrency) {
        this.prepaidCurrency = prepaidCurrency;
    }

    public RateTable getRateTable() {
        return rateTable;
    }

    public void setRateTable(RateTable rateTable) {
        this.rateTable = rateTable;
    }

    public void setCanOrder(boolean canOrder) {
        this.canOrder = canOrder;
    }

    public boolean isCanOrder() {
        return canOrder;
    }

    public boolean isChargeVAT() {
        return chargeVAT;
    }

    public void setChargeVAT(boolean chargeVAT) {
        this.chargeVAT = chargeVAT;
    }

    public Price getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Price minAmount) {
        this.minAmount = minAmount;
    }
}
