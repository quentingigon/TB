package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="schedule", schema="public")
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="schedule_id")
	private Integer id;

	@Column(name="name")
	private String name;

	@ElementCollection
	private List<Integer> fluxes;

	@ElementCollection
	private List<Integer> fallbacks;

	public Schedule() {
		fallbacks = new ArrayList<>();
		fluxes = new ArrayList<>();
	}

	public Schedule(String name) {
		this.name = name;
		fallbacks = new ArrayList<>();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Integer> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<Integer> fluxes) {
		this.fluxes = fluxes;
	}

	public void addToFluxes(Integer fluxId) {
		if (this.fluxes == null) {
			this.fluxes = new ArrayList<>();
		}
		this.fluxes.add(fluxId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getFallbacks() {
		return fallbacks;
	}

	public void setFallbacks(List<Integer> fallbacks) {
		this.fallbacks = fallbacks;
	}
}
