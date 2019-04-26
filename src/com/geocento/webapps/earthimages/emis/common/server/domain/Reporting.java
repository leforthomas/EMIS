package com.geocento.webapps.earthimages.emis.common.server.domain;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Reporting {

    static public enum TYPE {ERROR};

	@Id
	@GeneratedValue
	Long id;

    @Enumerated(EnumType.STRING)
    TYPE type;
    @Column(length=500)
	String title;
	@Column(length=50000)
	String content;
	@Temporal(TemporalType.TIMESTAMP)
	Date date;
	
	public Reporting() {
	}

	public Reporting(TYPE type, String title, String content, Date date) {
		super();
        this.type = type;
		this.title = title;
		this.content = content;
		this.date = date;
	}

	public Long getId() {
		return id;
	}

    public TYPE getType() {
        return type;
    }

    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
}
