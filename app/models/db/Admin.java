package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for Admin.
 */
@Entity
@IdClass(AdminId.class)
@Table(name="admin", schema = "public")
public class Admin {

	@Id
	@Column(name="user_id")
	private Integer userId;

	public Admin() {
	}

	public Admin(Integer userId) {
		this.userId = userId;
	}

	public Admin(User user) {
		this.userId = user.getId();
	}

	// Getter and setters

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
