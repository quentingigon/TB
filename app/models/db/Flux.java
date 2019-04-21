package models.db;

import javax.persistence.*;

@Entity
@Table(name="fluxes", schema="public")
public class Flux {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="name")
	private String name;

	@Column(name="duration")
	private int duration;

	@Column(name="url")
	private String url;

	public Flux(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public Flux() {
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
}