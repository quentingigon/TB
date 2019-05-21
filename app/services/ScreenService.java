package services;

import models.db.Screen;
import models.db.WaitingScreen;
import models.entities.ScreenData;
import models.repositories.ScreenRepository;

import java.util.ArrayList;
import java.util.List;

public class ScreenService {

	private final ScreenRepository screenRepository;

	public ScreenService(ScreenRepository screenRepository) {
		this.screenRepository = screenRepository;
	}

	public Screen create(Screen screen) {
		return screenRepository.add(screen);
	}

	public Screen update(Screen screen) {
		return screenRepository.update(screen);
	}

	public WaitingScreen createWS(WaitingScreen screen) {
		return screenRepository.add(screen);
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
