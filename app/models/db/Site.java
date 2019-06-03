package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for Site.
 */
@Entity
@Table(name="site", schema="public")
public class Site {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="site_id")
	private Integer id;

	@Column(name="name")
	private String name;

	public Site() {
	}

	public Site(String name) {
		this.name = name;
	}

	// Getters and setters

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
