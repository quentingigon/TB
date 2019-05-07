package models.db;

import javax.persistence.*;

@Entity
@Table(name="generalflux", schema="public")
public class GeneralFlux {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="generalflux_id")
	private Integer id;

	@Column(name="flux_id")
	private Integer fluxId;

	public GeneralFlux() {
	}

	public GeneralFlux(Integer fluxId) {
		this.fluxId = fluxId;
	}

	public Integer getId() {
		return id;
	}

	public Integer getFluxId() {
		return fluxId;
	}

	public void setFluxId(Integer fluxId) {
		this.fluxId = fluxId;
	}
}
