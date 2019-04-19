package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="screengroups", schema="public")
public class ScreenGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@OneToMany
	@Column(name="screens")
	private List<Screen> screens;

	public ScreenGroup() {
		this.screens = new ArrayList<>();
	}

	public Integer getId() {
		return id;
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}

	public void addToScreens(Screen screen) {
		this.screens.add(screen);
	}
}
