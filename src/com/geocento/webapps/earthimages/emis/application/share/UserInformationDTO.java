package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;

public class UserInformationDTO implements Serializable {

    private String profilePicture;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String company;
    private String address;
    private String phone;
    private String countryCode;
    boolean needsVATNumber;
    String communityVATNumber;

    public UserInformationDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public boolean isNeedsVATNumber() {
        return needsVATNumber;
    }

    public void setNeedsVATNumber(boolean needsVATNumber) {
        this.needsVATNumber = needsVATNumber;
    }

    public String getCommunityVATNumber() {
        return communityVATNumber;
    }

    public void setCommunityVATNumber(String communityVATNumber) {
        this.communityVATNumber = communityVATNumber;
    }
}
