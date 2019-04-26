package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class ImageAlert implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

    public static String productIdSeparator = "<>";

    @Id
	@GeneratedValue
	Long id;
    @Basic
    String name;
    @ManyToOne
    User owner;
    @Column(length = 100)
    String searchId;
    @Temporal(TemporalType.TIMESTAMP)
    Date endDate;
    @ManyToOne
    Order order;
    @Temporal(TemporalType.TIMESTAMP)
	Date creationTime;
    @Temporal(TemporalType.TIMESTAMP)
	Date lastViewed;

    // needed to run the alerts
    // period in hours between search updates
    @Basic
    int updatePeriod = 24;
    @Basic
    int newProducts = 0;
    @Basic
    int totalProducts = 0;
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
	Date fetchDate;
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    Date lastQueried;
    // stores the list of product ids from the previous search
    // used to filter out the non new product ids
    @Column(length = 10000)
    private String previousProductIds;

    // create an email alert without user registration
    // results are sent by email and the email includes a unotify link
	public ImageAlert() {
		creationTime = new Date();
		lastViewed = creationTime;
		fetchDate = creationTime;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User user) {
        this.owner = user;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Date lastViewed) {
        this.lastViewed = lastViewed;
    }

    public int getUpdatePeriod() {
        return updatePeriod;
    }

    public void setUpdatePeriod(int updatePeriod) {
        this.updatePeriod = updatePeriod;
    }

    public int getNewProducts() {
        return newProducts;
    }

    public void setNewProducts(int newProducts) {
        this.newProducts = newProducts;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Date getFetchDate() {
        return fetchDate;
    }

    public void setFetchDate(Date fetchDate) {
        this.fetchDate = fetchDate;
    }

    public Date getLastQueried() {
        return lastQueried;
    }

    public void setLastQueried(Date lastQueried) {
        this.lastQueried = lastQueried;
    }

    public String getPreviousProductIds() {
        return previousProductIds;
    }

    public void setPreviousProductIds(String previousProductIds) {
        this.previousProductIds = previousProductIds;
    }
}
