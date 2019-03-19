package models.repositories;

import com.google.inject.ImplementedBy;
import models.Screen;

@ImplementedBy(JPAScreenRepository.class)
public interface ScreenRepository {

	Screen getByMacAddress(String address);
	void add(Screen screen);
}
