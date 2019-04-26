package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListContactsResponse {

    @JsonProperty("vid-offset")
    Long vidOffset;

    @JsonProperty("has-more")
    boolean hasMore;

    List<Contact> contacts;

    public ListContactsResponse() {
    }

    public Long getVidOffset() {
        return vidOffset;
    }

    public void setVidOffset(Long vidOffset) {
        this.vidOffset = vidOffset;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }
}
