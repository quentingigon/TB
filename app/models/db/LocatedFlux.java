package models.db;

import javax.persistence.*;

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
	private Integer siteI;

	public LocatedFlux() {
	}

	public LocatedFlux(Integer fluxId, Integer siteI) {
		this.fluxId = fluxId;
		this.siteI = siteI;
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

	public Integer getSiteI() {
		return siteI;
	}

	public void setSiteI(Integer siteI) {
		this.siteI = siteI;
	}
}
