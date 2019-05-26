import models.db.Team;
import models.repositories.interfaces.ScreenRepository;
import models.repositories.interfaces.TeamRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.TeamService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TeamUnitTest {

	@Mock
	private ScreenRepository mockScreenRepository;

	@Mock
	private TeamRepository mockTeamRepository;

	private String teamName;
	private int teamId;
	private Team teamToReturn;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		teamName = "test";
		teamId = 42;
		teamToReturn = new Team(teamName);
	}

	// Creation
	@Test
	public void teamCreation() {
		when(mockTeamRepository.add(any(Team.class))).thenReturn(teamToReturn);
		TeamService ts = new TeamService(mockTeamRepository);

		assertEquals(ts.create(teamToReturn), teamToReturn);
	}

	// Update
	@Test
	public void teamUpdate() {
		when(mockTeamRepository.update(any(Team.class))).thenReturn(teamToReturn);
		TeamService ts = new TeamService(mockTeamRepository);

		assertEquals(ts.update(teamToReturn), teamToReturn);
	}

	// Getters

	@Test
	public void testGetTeamByName() {
		when(mockTeamRepository.getByName(teamName)).thenReturn(teamToReturn);
		TeamService ts = new TeamService(mockTeamRepository);

		assertEquals(teamName, ts.getTeamByName(teamName).getName());
	}

	@Test
	public void testGetTeamByNameFail() {
		when(mockTeamRepository.getByName(teamName)).thenReturn(teamToReturn);
		TeamService ts = new TeamService(mockTeamRepository);

		assertNull(ts.getTeamByName("WrongName"));
	}

	@Test
	public void testGetTeamById() {
		when(mockTeamRepository.getById(teamId)).thenReturn(teamToReturn);
		TeamService ts = new TeamService(mockTeamRepository);

		assertEquals(teamName, ts.getTeamById(teamId).getName());
	}

	@Test
	public void testGetTeamByIdFail() {
		when(mockTeamRepository.getByName(teamName)).thenReturn(teamToReturn);
		TeamService ts = new TeamService(mockTeamRepository);

		assertNull(ts.getTeamById(43));
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
