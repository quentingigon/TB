package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="runningdiffusers", schema="public")
@DiscriminatorValue("test")
public class RunningDiffuser extends Diffuser {

	@PrimaryKeyJoinColumn
	@Column(name="flux_id")
	private Integer fluxId;

	@OneToMany
	@Column(name="screens")
	private List<Screen> screens;

	public RunningDiffuser() {

	}

	public RunningDiffuser(Diffuser d) {
		setName(d.getName());
		setValidity(d.getValidity());
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
