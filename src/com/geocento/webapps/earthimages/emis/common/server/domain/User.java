package com.geocento.webapps.earthimages.emis.common.server.domain;

import com.geocento.webapps.earthimages.emis.common.share.entities.DOMAIN;
import com.geocento.webapps.earthimages.emis.common.share.entities.USAGE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_STATUS;
import com.google.gwt.user.client.rpc.GwtTransient;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by thomas on 9/01/15.
 */
@Entity
// user is a reserved name for tables and cannot be used
@Table(name = "USERS", uniqueConstraints=@UniqueConstraint(columnNames={"username", "email"}))
public class User {

    @Id
    @Column(name="username")
    private String username;

    @Column(length=100)
    private String firstName;

    @Column(length=100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    private USER_ROLE userRole;

    @Basic
    @Enumerated(EnumType.STRING)
    private USER_STATUS userStatus;

    @Basic
    @GwtTransient private String passwordHash;

    // 2 letters country codes as per ISO 3166-1 alpha-2 codes
    @Basic
    @Column(length=2)
    private String countryCode;

    @Column(name="email", unique=true)
    private String email;

    @Basic
    @Column(length=255)
    String company;

    @Column(length=1000)
    String address;

    @Column(length=100)
    String phone;

    @Enumerated(EnumType.STRING)
    DOMAIN domain;

    @Enumerated(EnumType.STRING)
    USAGE usage;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    Date registeredDate;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    Date lastLoggedIn;

    // user's settings
    @OneToOne(fetch=FetchType.EAGER, mappedBy = "owner", cascade={CascadeType.ALL})
    UserSettings settings;

    @OneToOne(fetch=FetchType.EAGER, mappedBy = "owner", cascade={CascadeType.ALL})
    UserCart userCart;

    @OneToOne(fetch=FetchType.EAGER, cascade={CascadeType.ALL})
    Credit credit;

    @OneToOne(fetch=FetchType.EAGER, mappedBy = "owner", cascade={CascadeType.ALL})
    UserLayers userLayer;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "owner", cascade={CascadeType.ALL})
    List<ImageAlert> imageAlerts;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "owner", cascade={CascadeType.ALL})
    List<Order> orders;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "owner", cascade={CascadeType.ALL})
    List<Workspace> workspaces;

    @OneToMany(fetch=FetchType.LAZY, mappedBy = "owner", cascade={CascadeType.ALL})
    List<Signature> signatures;

    boolean canOrder;

    boolean chargeVAT;
    boolean needsVATNumber;
    @Column(length=100)
    String communityVATNumber;

    public User() {
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings;
    }

    public void setUserCart(UserCart userCart) {
        this.userCart = userCart;
    }

    public UserCart getUserCart() {
        return userCart;
    }

    public Credit getCredit() {
        return credit;
    }

    public void setCredit(Credit credit) {
        this.credit = credit;
    }

    public UserLayers getUserLayer() {
        return userLayer;
    }

    public void setUserLayer(UserLayers userLayer) {
        this.userLayer = userLayer;
    }

    public List<ImageAlert> getImageAlerts() {
        return imageAlerts;
    }

    public void setImageAlerts(List<ImageAlert> imageAlerts) {
        this.imageAlerts = imageAlerts;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> users) {
        this.orders = users;
    }

    public List<Workspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<Workspace> workspaces) {
        this.workspaces = workspaces;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<Signature> signatures) {
        this.signatures = signatures;
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

    public boolean isCanOrder() {
        return canOrder;
    }

    public void setCanOrder(boolean canOrder) {
        this.canOrder = canOrder;
    }

    public boolean isChargeVAT() {
        return chargeVAT;
    }

    public void setChargeVAT(boolean chargeVAT) {
        this.chargeVAT = chargeVAT;
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
