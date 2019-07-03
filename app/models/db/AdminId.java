package models.db;

import java.io.Serializable;
import java.util.Objects;

public class AdminId implements Serializable {

	private Integer userId;

	public AdminId() {
	}

	public AdminId(Integer userId) {
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AdminId adminId = (AdminId) o;
		return userId.equals(adminId.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}
}
