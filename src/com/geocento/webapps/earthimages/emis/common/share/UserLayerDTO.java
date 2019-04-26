package com.geocento.webapps.earthimages.emis.common.share;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by thomas on 27/03/2015.
 */
public class UserLayerDTO implements Serializable {

    Long id;

    LayerDTO layerDTO;

    boolean visible;
    int opacity;
    int zIndex;

    Date selectedTime;

    public UserLayerDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LayerDTO getLayerDTO() {
        return layerDTO;
    }

    public void setLayerDTO(LayerDTO layerDTO) {
        this.layerDTO = layerDTO;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public Date getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(Date selectedTime) {
        this.selectedTime = selectedTime;
    }
}
