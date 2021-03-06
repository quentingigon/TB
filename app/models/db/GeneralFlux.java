package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for GeneralFlux.
 */
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

	// Getters and setters

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
