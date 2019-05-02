package models.db;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="diffusers", schema="public")
public class Diffuser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="name")
	private String name;

	@Column(name="validity")
	private Date validity;

	public Diffuser() {
	}

	public Diffuser(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public Date getValidity() {
		return validity;
	}

	public void setValidity(Date validity) {
		this.validity = validity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
