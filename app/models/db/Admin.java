package models.db;

import javax.persistence.Entity;

@Entity
public class Admin extends User {

	public Admin() {
		super();
	}

	public Admin(String email, String password) {
		super(email, password);
	}
}
