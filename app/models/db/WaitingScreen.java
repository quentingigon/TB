package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for WaitingScreen.
 */
@Entity
@Table(name="waitingscreen", schema="public")
public class WaitingScreen {

	@Id
	@Column(name="screen_id")
	private Integer screenId;

	@Column(name="code")
	private String code;

	public WaitingScreen(Integer screenId, String code) {
		this.screenId = screenId;
		this.code = code;
	}

	public WaitingScreen() {
	}

	// Getters and setters


	public Integer getScreenId() {
		return screenId;
	}

	public void setScreenId(Integer screenId) {
		this.screenId = screenId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
