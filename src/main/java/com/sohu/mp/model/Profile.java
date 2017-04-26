package com.sohu.mp.model;

public class Profile {
	
	private Long id;
	private String userName;
	private String description;
	private String avatorUrl;
	private String passport;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAvatorUrl() {
		return avatorUrl;
	}
	public void setAvatorUrl(String avatorUrl) {
		this.avatorUrl = avatorUrl;
	}
	public String getPassport() {
		return passport;
	}
	public void setPassport(String passport) {
		this.passport = passport;
	}
	
	
}
