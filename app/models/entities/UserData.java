package models.entities;

import play.data.validation.Constraints;

public class UserData {

	@Constraints.Required
	private String email;

	@Constraints.Required
	private String password;

	public UserData() {
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
}
