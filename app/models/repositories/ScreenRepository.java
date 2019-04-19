package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Screen;

@ImplementedBy(JPAScreenRepository.class)
public interface ScreenRepository {

	Screen getByMacAddress(String address);
	void add(Screen screen);
}
