package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.List;


@Entity
public class ProductSample {

    @Id
	String id;

    @Column(length = 1000)
    String title;

    @Column(length = 1000)
    String description;

    @ElementCollection
    List<String> keywords;

    // the basic product values
    Long instrumentId;
    Long modeId;

    @OneToOne
    ProductOrder productOrder;

    public ProductSample() {
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getModeId() {
        return modeId;
    }

    public void setModeId(Long modeId) {
        this.modeId = modeId;
    }

    public ProductOrder getProductOrder() {
        return productOrder;
    }

    public void setProductOrder(ProductOrder productOrder) {
        this.productOrder = productOrder;
    }
}