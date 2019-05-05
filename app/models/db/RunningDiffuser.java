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

	@Column(name="flux_id")
	private Integer fluxId;

	@OneToMany
	@Column(name="screens")
	private List<Screen> screens;

	public RunningDiffuser() {

	}

	public RunningDiffuser(Diffuser d) {
		// setName(d.getName());
		// setValidity(d.getValidity());
	}

	public Integer getFluxId() {
		return fluxId;
	}

	public void setFluxId(Integer flux_id) {
		this.fluxId = flux_id;
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}

	public void addToScreens(Screen s) {
		if (screens == null)
			screens = new ArrayList<>();
		screens.add(s);
	}
}
