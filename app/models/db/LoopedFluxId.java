package models.db;

import java.io.Serializable;
import java.util.Objects;

public class LoopedFluxId implements Serializable {

	private Integer fluxLoopId;
	private Integer fluxId;

	public LoopedFluxId() {
	}

	public LoopedFluxId(Integer fluxLoopId, Integer fluxId) {
		this.fluxLoopId = fluxLoopId;
		this.fluxId = fluxId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LoopedFluxId that = (LoopedFluxId) o;
		return fluxLoopId.equals(that.fluxLoopId) &&
			fluxId.equals(that.fluxId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fluxLoopId, fluxId);
	}
}
