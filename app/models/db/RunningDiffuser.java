package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the DM model for RunningDiffuser.
 */
@Entity
@Table(name="runningdiffuser", schema="public")
public class RunningDiffuser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="runningdiffuser_id")
	private Integer id;

	@Column(name="diffuser_id")
	private Integer diffuserId;

	@Column(name="flux_id")
	private Integer fluxId;

	@ElementCollection
	private List<Integer> screens;

	public RunningDiffuser() {

	}

	public RunningDiffuser(Diffuser d) {
		diffuserId = d.getId();
	}

	// Getters and setters

	public Integer getId() {
		return id;
	}

	public List<Integer> getScreens() {
		return screens;
	}

	public void setScreens(List<Integer> screens) {
		this.screens = screens;
	}

	public void addToScreens(Integer screenId) {
		if (screens == null)
			screens = new ArrayList<>();
		screens.add(screenId);
	}

	public Integer getDiffuserId() {
		return diffuserId;
	}

	public void setDiffuserId(Integer diffuserId) {
		this.diffuserId = diffuserId;
	}

	public Integer getFluxId() {
		return fluxId;
	}

	public void setFluxId(Integer fluxId) {
		this.fluxId = fluxId;
	}
}
