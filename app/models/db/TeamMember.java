package models.db;

import javax.persistence.Entity;

@Entity
public class TeamMember extends User {

	public TeamMember() {
		super();
	}

	public TeamMember(String email, String password) {
		super(email, password);
	}
}
