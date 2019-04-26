package com.geocento.webapps.earthimages.emis.common.share.entities;

import com.metaaps.webapps.libraries.client.widget.util.Utils;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Inheritance(strategy= InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="type")
public abstract class AOI implements Serializable {

    @Id
    @GeneratedValue
    Long id;

    @Column(length = 100)
	String name;
    boolean visible;
    @Column(length = 10)
	String fillColor;
    @Column(length = 10)
	String strokeColor;
	int strokeThickness;
	double strokeOpacity;
	double fillOpacity;

    public AOI() {
	}

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if(name != null) {
			this.name = name.trim();
		}
	}

    public boolean isVisible() {
        return visible;
    }

    // makes sure values are correct
	protected void checkValues() {
		if(strokeColor == null) {
			strokeColor = Utils.nextDiscreteColor();
		}
		if(strokeThickness == 0) {
			strokeThickness = 2;
		}
		if(strokeOpacity == 0.0) {
			strokeOpacity = 1.0;
		}
		if(fillColor == null) {
			fillColor = Utils.invertColor(strokeColor);
		}
		if(fillOpacity == 0.0) {
			fillOpacity = 0.5;
		}
	}

	public String getStrokeColor() {
		return strokeColor;
	}
	
	public String getFillColor() {
		return fillColor;
	}
	
	public void setStrokeColor(String strokeColor) {
		this.strokeColor = strokeColor;
	}
	
	public void setFillColor(String fillColor) {
		this.fillColor = fillColor;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getStrokeThickness() {
		return strokeThickness;
	}

	public void setStrokeThickness(int strokeThickness) {
		this.strokeThickness = strokeThickness;
	}

	public double getStrokeOpacity() {
		return strokeOpacity;
	}

	public void setStrokeOpacity(double strokeOpacity) {
		this.strokeOpacity = strokeOpacity;
	}

	public double getFillOpacity() {
		return fillOpacity;
	}

	public void setFillOpacity(double fillOpacity) {
		this.fillOpacity = fillOpacity;
	}
}
