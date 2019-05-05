package models.db;

import javax.persistence.*;

@Entity
@Table(name="user", schema="public")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="user_id")
	private Integer id;

	@Column(name="email")
	private String email;
	@Column(name="password")
	private String password;

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public User() {}

	public Integer getId() {
		return id;
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
