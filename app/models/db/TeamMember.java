package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for TeamMember.
 */
@Entity
@IdClass(TeamMemberId.class)
@Table(name="teammember", schema="public")
public class TeamMember {

	@Id
	@Column(name="user_id")
	private Integer userId;

	@Id
	@Column(name = "team_id")
	private Integer teamId;

	public TeamMember() {
	}

	public TeamMember(User user) {
		userId = user.getId();
	}

	public TeamMember(String email, String password) {
	}

	// Getters and setters


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
