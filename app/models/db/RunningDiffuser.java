package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="runningdiffuser", schema="public")
public class RunningDiffuser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="runningdiffuser_id")
	private Integer id;

	@Column(name="diffuser_id")
	private Integer diffuserId;

	@ElementCollection
	private List<Integer> screens;

	public RunningDiffuser() {

	}

	public RunningDiffuser(Diffuser d) {
		diffuserId = d.getId();
	}

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
}
