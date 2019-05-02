package models.db;

import javax.persistence.*;

@Entity
@Table(name="diffusers", schema="public")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="name")
public class Diffuser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="name")
	private String name;

	@Column(name="validity")
	private Integer validity;

	public Diffuser() {
	}

	public Diffuser(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public Integer getValidity() {
		return validity;
	}

	public void setValidity(Integer validity) {
		this.validity = validity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
