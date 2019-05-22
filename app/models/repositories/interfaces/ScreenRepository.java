package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.Screen;
import models.db.WaitingScreen;
import models.repositories.JPAScreenRepository;

import java.util.List;

@ImplementedBy(JPAScreenRepository.class)
public interface ScreenRepository {

	Screen getByMacAddress(String address);
	Screen getById(int id);

	List<Screen> getAll();
	List<Integer> getAllScreenIdsOfTeam(Integer id);
	List<Screen> getAllByRunningScheduleId(Integer id);

	Screen add(Screen screen);
	Screen update(Screen screen);
	void delete(Screen screen);


	WaitingScreen add(WaitingScreen waitingScreen);
	void delete(WaitingScreen waitingScreen);
	WaitingScreen getByMac(String mac);
}
