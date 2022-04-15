package com.gnc.task.application.data.entity;

import com.gnc.task.application.data.AbstractEntity;

import javax.persistence.*;

@Entity
@Table(name = "user_configs")
public class UserConfig extends AbstractEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	private String theme;

	public UserConfig() {
	}

	public UserConfig(User user, String theme) {
		this.user = user;
		this.theme = theme;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
}
