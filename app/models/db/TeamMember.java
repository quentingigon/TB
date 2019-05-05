package models.db;

import javax.persistence.*;

@Entity
@Table(name="teammember", schema="public")
public class TeamMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="member_id")
	private Integer id;

	@Column(name="user_id")
	private Integer userId;

	@Column(name = "team_id")
	private Integer teamId;

	public TeamMember() {
	}

	public TeamMember(User user) {
		// setEmail(user.getEmail());
		// setPassword(user.getPassword());
	}

	public TeamMember(String email, String password) {
	}

	public Integer getTeamId() {
		return teamId;
	}

	public void setTeamId(Integer team) {
		this.teamId = team;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
