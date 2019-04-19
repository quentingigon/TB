package models.db;

import javax.persistence.*;

@Entity
@Table(name="screens", schema = "public")
public class Screen {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="mac")
	private String macAddress;

	@ManyToOne
	private Site site;

	@Column(name="logged")
	private boolean logged;

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

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
	}
}
