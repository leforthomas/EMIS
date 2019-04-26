package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_STATUS;

import java.io.Serializable;
import java.util.Date;


public class UserSession implements Serializable {
	
    private String userName;
    private USER_ROLE userType;
    private Date loginTime;
    private USER_STATUS userStatus;

	public UserSession() {
	}
	
    public UserSession(User user) {
    	loginTime = new Date();
        userName = user.getUsername();
        userType = user.getUserRole();
        userStatus = user.getUserStatus();
	}

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public String getUserName() {
        return userName;
    }

    public USER_ROLE getUserType() {
        return userType;
    }

    public USER_STATUS getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(USER_STATUS userStatus) {
        this.userStatus = userStatus;
    }
}
