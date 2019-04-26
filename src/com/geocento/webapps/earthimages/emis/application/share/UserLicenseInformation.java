package com.geocento.webapps.earthimages.emis.application.share;

import java.io.Serializable;
import java.util.List;

public class UserLicenseInformation implements Serializable {

    String userName;
    String organisation;
    List<String> signatureUrls;

    public UserLicenseInformation() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public List<String> getSignatureUrls() {
        return signatureUrls;
    }

    public void setSignatureUrls(List<String> signatureUrls) {
        this.signatureUrls = signatureUrls;
    }
}
