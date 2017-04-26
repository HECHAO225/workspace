package com.sohu.mp.model;

import java.sql.Timestamp;

public class News {
	
	private Long id;
	private String title;
	private String mpMediaId;
	private String auditWords;
	private Integer mediaType;
	private Timestamp postTime;
	private String userName;
	private Long cmsId;
	
    public Long getCmsId() {
        return cmsId;
    }

    public void setCmsId(Long cmsId) {
        this.cmsId = cmsId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
	

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String tile) {
		this.title = tile;
	}
	public String getMpMediaId() {
		return mpMediaId;
	}
	public void setMpMediaId(String mpMediaId) {
		this.mpMediaId = mpMediaId;
	}
	public String getAuditWords() {
		return auditWords;
	}
	public void setAuditWords(String auditWords) {
		this.auditWords = auditWords;
	}
	public Integer getMediaType() {
		return mediaType;
	}
	public void setMediaType(Integer mediaType) {
		this.mediaType = mediaType;
	}
	public Timestamp getPostTime() {
		return postTime;
	}
	public void setPostTime(Timestamp postTime) {
		this.postTime = postTime;
	}
	
}
