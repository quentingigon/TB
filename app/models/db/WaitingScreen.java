package models.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="waitingscreens", schema="public")
public class WaitingScreen {

	@Id
	@Column(name="mac")
	private String mac;

	@Column(name="code")
	private String code;

	public WaitingScreen(String mac, String code) {
		this.mac = mac;
		this.code = code;
	}

	public WaitingScreen() {
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
