package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="schedules", schema="public")
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="name")
	protected String name;

	@OneToMany
	@Column(name="fluxes")
	protected List<Flux> fluxes;

	public Schedule() {
		fluxes = new ArrayList<>();
	}

	public Schedule(String name) {
		this.name = name;
		fluxes = new ArrayList<>();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Flux> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<Flux> fluxes) {
		this.fluxes = fluxes;
	}

	public void addToFluxs(Flux flux) {
		if (this.fluxes == null) {
			this.fluxes = new ArrayList<>();
		}
		this.fluxes.add(flux);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
