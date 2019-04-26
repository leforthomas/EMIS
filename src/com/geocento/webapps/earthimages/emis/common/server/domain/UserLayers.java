package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class UserLayers {

    @Id
    @GeneratedValue
    Long id;

    @OneToOne
    User owner;

    // list of the cart product requests
    @OneToMany(mappedBy = "userLayers", fetch= FetchType.EAGER, cascade= CascadeType.ALL)
    private List<UserLayer> layers = new ArrayList<UserLayer>();

    // last update of the workspace by the user
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    public UserLayers() {
        lastUpdate = new Date();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<UserLayer> getLayers() {
        return layers;
    }

    public void removeLayer(UserLayer layer) {
        layers.remove(layer);
    }
}
