package models.db;

import javax.persistence.*;

@Entity
@Table(name="waitingscreen", schema="public")
public class WaitingScreen {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="waitingscreen_id")
	private Integer id;

	@Column(name="mac_address")
	private String macAddress;

	@Column(name="code")
	private String code;

	public WaitingScreen(String code, String macAddress) {
		this.code = code;
		this.macAddress = macAddress;
	}

	public WaitingScreen() {
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
}
