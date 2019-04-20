package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.WaitingScreen;

@ImplementedBy(JPAWaitingScreenRepository.class)
public interface WaitingScreenRepository {

	WaitingScreen add(WaitingScreen waitingScreen);
	void delete(WaitingScreen waitingScreen);
	WaitingScreen getByMac(String mac);
}
