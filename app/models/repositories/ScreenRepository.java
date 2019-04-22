package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Screen;

@ImplementedBy(JPAScreenRepository.class)
public interface ScreenRepository {

	Screen getByMacAddress(String address);
	Screen getById(int id);
	void add(Screen screen);
	void update(Screen screen);
	void delete(Screen screen);
}
