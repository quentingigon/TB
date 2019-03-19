package models;

import javax.persistence.*;

@Entity
@Table(name="screens")
public class Screen {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private Integer id;

	@Column(name="mac")
	private String macAddress;

	public Screen(String macAddress) {
		this.macAddress = macAddress;
	}

	public Screen() {}

	public Integer getId() {
		return id;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
}
