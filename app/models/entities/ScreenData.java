package models.entities;

public class ScreenData {

	private String name;
	private String mac;
	private String code;
	private String site;
	private String resolution;

	public ScreenData() {
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

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
}
