package services;

import models.db.Screen;
import models.db.WaitingScreen;
import models.entities.ScreenData;
import models.repositories.interfaces.ScreenRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the service used to make operations on the database for Screens.
 */
public class ScreenService {

	private final ScreenRepository screenRepository;

	public ScreenService(ScreenRepository screenRepository) {
		this.screenRepository = screenRepository;
	}

	public Screen getScreenByMacAddress(String mac) {
		return screenRepository.getByMacAddress(mac);
	}

	public Screen getScreenById(Integer id) {
		return screenRepository.getById(id);
	}

	public Screen create(Screen screen) {
		return screenRepository.add(screen);
	}

	public Screen update(Screen screen) {
		return screenRepository.update(screen);
	}

	public void delete(Screen screen) {
		screenRepository.delete(screen);
	}

	public WaitingScreen getWSByMacAddress(String mac) {
		return screenRepository.getByMac(mac);
	}

	public WaitingScreen createWS(Screen screen, String code) {
		screen = create(screen);
		WaitingScreen ws = new WaitingScreen(screen.getId(), code);
		return screenRepository.add(ws);
	}

	public void delete(WaitingScreen ws) {
		screenRepository.delete(ws);
	}

	public List<ScreenData> getAllScreens() {
		List<ScreenData> data = new ArrayList<>();
		for (Screen s: screenRepository.getAll()) {
			data.add(new ScreenData(s));
		}
		return data;
	}

	public List<ScreenData> getAllScreensOfTeam(int teamId) {
		List<ScreenData> data = new ArrayList<>();
		for (Integer screenId : screenRepository.getAllScreenIdsOfTeam(teamId)) {
			if (screenRepository.getById(screenId) != null) {
				data.add(new ScreenData(screenRepository.getById(screenId)));
			}
		}
		return data;
	}

	public List<ScreenData> getAllActiveScreensOfTeam(int teamId) {
		List<ScreenData> data = new ArrayList<>();
		for (Integer screenId : screenRepository.getAllScreenIdsOfTeam(teamId)) {
			Screen screen = screenRepository.getById(screenId);
			// screen has an associated RunningSchedule so it's active
			if (screen != null && screen.getRunningscheduleId() != null) {
				data.add(new ScreenData(screenRepository.getById(screenId)));
			}
		}
		return data;
	}
}
