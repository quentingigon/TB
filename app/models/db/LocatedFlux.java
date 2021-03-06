package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for LocatedFlux.
 */
@Entity
@Table(name="locatedflux", schema = "public")
public class LocatedFlux {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="locatedflux_id")
	private Integer id;

	@Column(name="flux_id")
	private Integer fluxId;

	@Column(name="site_id")
	private Integer siteId;

	public LocatedFlux() {
	}

	public LocatedFlux(Integer fluxId, Integer siteId) {
		this.fluxId = fluxId;
		this.siteId = siteId;
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

	public Integer getSiteId() {
		return siteId;
	}

	public void setSiteId(Integer siteI) {
		this.siteId = siteI;
	}
}
