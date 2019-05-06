package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="screengroup", schema="public")
public class ScreenGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="group_id")
	private Integer id;

	@ElementCollection
	private List<Integer> screens;

	public ScreenGroup() {
		this.screens = new ArrayList<>();
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

	public void addToScreens(Integer screen) {
		this.screens.add(screen);
	}
}
