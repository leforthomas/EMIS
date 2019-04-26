package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Signature {

    @Id
    @Column(length = 16)
    String id;

    @ManyToOne
    User owner;

    @Column(length = 1000)
    String path;

    @Temporal(TemporalType.TIMESTAMP)
    Date creationTime;

    public Signature() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
}
