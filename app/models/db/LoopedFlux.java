package models.db;

import javax.persistence.*;

@Entity
@IdClass(LoopedFluxId.class)
@Table(name="fluxloop_fluxes", schema = "public")
public class LoopedFlux {

	@Id
	@Column(name="fluxloop_id")
	private Integer fluxLoopId;

	@Id
	@Column(name="fluxes")
	private Integer fluxId;

	@Column(name="flux_order")
	private Integer order;

	public LoopedFlux() {
	}

	public Integer getFluxLoopId() {
		return fluxLoopId;
	}

	public void setFluxLoopId(Integer fluxLoopId) {
		this.fluxLoopId = fluxLoopId;
	}

	public Integer getFluxId() {
		return fluxId;
	}

	public void setFluxId(Integer fluxId) {
		this.fluxId = fluxId;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}
}
