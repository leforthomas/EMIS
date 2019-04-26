package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.server.utils.BCrypt;

import javax.persistence.*;
import java.util.Date;

@Entity
public class UserResetToken {

	@Id
	@GeneratedValue
	Long id;
	
	@Basic
	String userName;
	
	@Basic
	String token;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date expiryDate;
	
	public UserResetToken() {
	}
	
	public UserResetToken(String userName, long expiryTimeMs) {
		this.userName = userName;
		// generate token
	    token = BCrypt.hashpw(userName + Math.random() + "" + new Date().getTime(), BCrypt.gensalt());
	    // generate expiry date
	    expiryDate = new Date(new Date().getTime() + expiryTimeMs);
	}
	
	public String getToken() {
		return token;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public Date getExpiryDate() {
		return expiryDate;
	}
	
}
