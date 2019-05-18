package models.db;

import javax.persistence.*;

@Entity
@Table(name="diffuser", schema="public")
public class Diffuser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="diffuser_id")
	private Integer id;

	@Column(name="name")
	private String name;

	@Column(name="validity")
	private Integer validity;

	@Column(name="flux_id")
	private Integer flux;

	@Column(name="start_block")
	private Integer startBlock;

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

	public Integer getFlux() {
		return flux;
	}

	public void setFlux(Integer flux) {
		this.flux = flux;
	}

	public Integer getStartBlock() {
		return startBlock;
	}

	public void setStartBlock(Integer startBlock) {
		this.startBlock = startBlock;
	}


}
