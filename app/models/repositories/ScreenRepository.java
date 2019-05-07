package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Screen;

import java.util.List;

@ImplementedBy(JPAScreenRepository.class)
public interface ScreenRepository {

	Screen getByMacAddress(String address);
	Screen getById(int id);

	List<Screen> getAll();
	List<Integer> getAllScreenIdsOfTeam(Integer id);

	void add(Screen screen);
	void update(Screen screen);
	void delete(Screen screen);
}
