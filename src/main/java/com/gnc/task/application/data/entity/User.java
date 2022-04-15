package com.gnc.task.application.data.entity;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.gnc.task.application.data.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;

import com.gnc.task.application.data.Role;

@Entity
@Table(name = "users")
public class User extends AbstractEntity {

	private String username;
	private String name;
	@JsonIgnore
	private String hashedPassword;
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Role> roles;
	@Lob
	private String profilePictureUrl;

	public User() {
	}

	public User(String username, String name, String hashedPassword, Set<Role> roles, String profilePictureUrl,
			UserConfig userConfig) {
		this.username = username;
		this.name = name;
		this.hashedPassword = hashedPassword;
		this.roles = roles;
		this.profilePictureUrl = profilePictureUrl;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHashedPassword() {
		return hashedPassword;
	}
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}
	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}
}
