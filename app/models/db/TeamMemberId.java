package models.db;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class TeamMemberId implements Serializable {

	@Id
	@Column(name = "user_id")
	private Integer userId;

	@Id
	@Column(name = "team_id")
	private Integer teamId;

	public TeamMemberId() {
	}

	public TeamMemberId(Integer userId, Integer teamId) {
		this.userId = userId;
		this.teamId = teamId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TeamMemberId that = (TeamMemberId) o;
		return userId.equals(that.userId) &&
			teamId.equals(that.teamId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, teamId);
	}
}
