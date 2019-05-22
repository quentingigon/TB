import models.db.Screen;
import models.db.WaitingScreen;
import models.entities.ScreenData;
import models.repositories.interfaces.ScreenRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.ScreenService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ScreenUnitTest {

	@Mock
	private ScreenRepository mockScreenRepository;

	private String macAddress;
	private int teamId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		macAddress = "test";
		teamId = 1;
	}

	@Test
	public void testCreateScreen() {
		Screen screenToReturn = new Screen(macAddress);
		when(mockScreenRepository.add(any(Screen.class))).thenReturn(screenToReturn);
		ScreenService service = new ScreenService(mockScreenRepository);

		Screen newScreen = new Screen(macAddress);

		assertEquals(screenToReturn.getMacAddress(), service.create(newScreen).getMacAddress());
	}

	@Test
	public void testUpdateScreen() {
		Screen screenToReturn = new Screen(macAddress);
		when(mockScreenRepository.update(any(Screen.class))).thenReturn(screenToReturn);
		ScreenService service = new ScreenService(mockScreenRepository);

		Screen newScreen = new Screen(macAddress);

		assertEquals(screenToReturn.getMacAddress(), service.update(newScreen).getMacAddress());
	}

	@Test
	public void testCreateWaitingScreen() {
		WaitingScreen screenToReturn = new WaitingScreen("123", macAddress);
		when(mockScreenRepository.add(any(WaitingScreen.class))).thenReturn(screenToReturn);
		ScreenService service = new ScreenService(mockScreenRepository);

		WaitingScreen newScreen = new WaitingScreen("123", macAddress);

		assertEquals(screenToReturn.getMacAddress(), service.createWS(newScreen).getMacAddress());
	}

	@Test
	public void testGetAllScreens() {

		List<Screen> screens = new ArrayList<>();
		screens.add(new Screen(macAddress));

		List<ScreenData> screenData = new ArrayList<>();
		screenData.add(new ScreenData(screens.get(0)));

		when(mockScreenRepository.getAll()).thenReturn(screens);
		ScreenService service = new ScreenService(mockScreenRepository);

		assertEquals(screenData.get(0).getMac(), service.getAllScreens().get(0).getMac());
	}

	@Test
	public void testGetAllScreensOfTeam() {
		List<Integer> screenIds = new ArrayList<>();
		int screenId = 42;
		screenIds.add(screenId);

		List<ScreenData> screenData = new ArrayList<>();
		screenData.add(new ScreenData(macAddress));

		when(mockScreenRepository.getAllScreenIdsOfTeam(teamId)).thenReturn(screenIds);
		when(mockScreenRepository.getById(any(Integer.class))).thenReturn(new Screen(macAddress));
		ScreenService service = new ScreenService(mockScreenRepository);

		assertEquals(screenData.get(0).getMac(), service.getAllScreensOfTeam(teamId).get(0).getMac());
	}


	@Test
	public void testGetAllActiveScreensOfTeam() {
		List<Integer> screenIds = new ArrayList<>();
		int screenId = 42;
		int activeScreenid = 43;
		screenIds.add(screenId);
		screenIds.add(activeScreenid);

		List<ScreenData> screenData = new ArrayList<>();
		screenData.add(new ScreenData(macAddress));

		Screen activeScreen = new Screen(macAddress);
		activeScreen.setRunningscheduleId(1);

		when(mockScreenRepository.getAllScreenIdsOfTeam(any(Integer.class))).thenReturn(screenIds);
		when(mockScreenRepository.getById(screenId)).thenReturn(null);
		when(mockScreenRepository.getById(activeScreenid)).thenReturn(activeScreen);
		ScreenService service = new ScreenService(mockScreenRepository);

		assertEquals(screenData.get(0).getMac(), service.getAllActiveScreensOfTeam(teamId).get(0).getMac());
	}
}
