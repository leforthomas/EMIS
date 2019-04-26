package com.geocento.webapps.earthimages.emis.common.server.utils.planetapi.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {

    List<AccountValues> accountValues;

    public AccountResponse() {
    }

    public List<AccountValues> getAccountValues() {
        return accountValues;
    }

    public void setAccountValues(List<AccountValues> accountValues) {
        this.accountValues = accountValues;
    }
}
