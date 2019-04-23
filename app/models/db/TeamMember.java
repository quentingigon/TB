package models.db;

import javax.persistence.*;

@Entity
@Table(name="teammembers", schema="public")
public class TeamMember extends User {

	@ManyToOne
	@JoinColumn(name = "team")
	private Team team;

	public TeamMember() {
	}

	public TeamMember(User user) {
		setEmail(user.getEmail());
		setPassword(user.getPassword());
	}

	public TeamMember(String email, String password) {
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
}
