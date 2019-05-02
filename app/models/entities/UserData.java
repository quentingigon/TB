package models.entities;

import models.db.User;
import play.data.validation.Constraints;

public class UserData {

	@Constraints.Required
	private String email;

	@Constraints.Required
	private String password;

	private String team;

	public UserData() {
	}

	public UserData(String email) {
		this.email = email;
	}

	public UserData(User u) {
		email = u.getEmail();
		password = u.getPassword();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}
}
