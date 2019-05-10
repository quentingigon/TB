package models.db;

import javax.persistence.*;

@Entity
@Table(name="admin", schema = "public")
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="admin_id")
	private Integer id;

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

	public Integer getId() {
		return id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
