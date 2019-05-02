package models.entities;

import models.db.Diffuser;

public class DiffuserData {

	private String name;

	public DiffuserData() {
	}

	public DiffuserData(String name) {
		this.name = name;
	}

	public DiffuserData(Diffuser diffuser) {
		name = diffuser.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
