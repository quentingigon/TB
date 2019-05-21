import models.db.Team;
import models.repositories.ScreenRepository;
import models.repositories.TeamRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.TeamService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TeamUnitTest {

	@Mock
	private ScreenRepository mockScreenRepository;

	@Mock
	private TeamRepository mockTeamRepository;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void teamCreation() {
		Team team = new Team("test");
		when(mockTeamRepository.add(any(Team.class))).thenReturn(team);
		TeamService ts = new TeamService(mockTeamRepository);

		assertEquals(ts.create(team), team);
	}

	@Test
	public void teamUpdate() {
		Team team = new Team("test");
		when(mockTeamRepository.update(any(Team.class))).thenReturn(team);
		TeamService ts = new TeamService(mockTeamRepository);

		assertEquals(ts.update(team), team);
	}

	@Test
	public void testGetAllScreensOfTeam_validId() {
		int teamId = 1;
		int screenId = 1;

		List<Integer> screenIds = new ArrayList<>();
		screenIds.add(screenId);
		when(mockScreenRepository.getAllScreenIdsOfTeam(teamId)).thenReturn(screenIds);

		List<Integer> screens = mockScreenRepository.getAllScreenIdsOfTeam(teamId);

		assertEquals(screenId, (int) screens.get(0));
		verify(mockScreenRepository).getAllScreenIdsOfTeam(teamId);
	}

	@Test
	public void testGetAllScreensOfTeam_invalidId() {
		int teamId = 1;
		int screenId = 1;

		List<Integer> screenIds = new ArrayList<>();
		screenIds.add(screenId);
		when(mockScreenRepository.getAllScreenIdsOfTeam(teamId)).thenReturn(screenIds);

		List<Integer> screens = mockScreenRepository.getAllScreenIdsOfTeam(teamId + 1);

		assertTrue(screens.isEmpty());
		verify(mockScreenRepository).getAllScreenIdsOfTeam(teamId + 1);
	}


}
