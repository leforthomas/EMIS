package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact {

    public String addedAt;
    public int vid;
    @JsonProperty("canonical-vid")
    public int canonicalVid;
    @JsonProperty("profile-url")
    public String profileUrl;
    @JsonProperty("profile-token")
    public String profileToken;
    @JsonProperty("is-contact")
    public boolean isContact;
    @JsonProperty("portal-id")
    public int portalId;

    //@JsonDeserialize(keyUsing = PropertiesDeserializer.class)
    public Map<String, Property> properties;

    public Contact() {
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }

    public int getVid() {
        return vid;
    }

    public void setVid(int vid) {
        this.vid = vid;
    }

    public int getCanonicalVid() {
        return canonicalVid;
    }

    public void setCanonicalVid(int canonicalVid) {
        this.canonicalVid = canonicalVid;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getProfileToken() {
        return profileToken;
    }

    public void setProfileToken(String profileToken) {
        this.profileToken = profileToken;
    }

    public boolean isContact() {
        return isContact;
    }

    public void setContact(boolean contact) {
        isContact = contact;
    }

    public int getPortalId() {
        return portalId;
    }

    public void setPortalId(int portalId) {
        this.portalId = portalId;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }
}
