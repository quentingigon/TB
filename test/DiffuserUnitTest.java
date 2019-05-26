import models.db.Diffuser;
import models.db.RunningDiffuser;
import models.entities.DiffuserData;
import models.repositories.interfaces.DiffuserRepository;
import models.repositories.interfaces.RunningDiffuserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.DiffuserService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DiffuserUnitTest {

	@Mock
	private DiffuserRepository mockDiffuserRepository;

	@Mock
	private RunningDiffuserRepository mockRunningDiffuserRepository;

	private String diffuserName;
	private int teamId;
	private int diffuserId = 42;
	private Diffuser diffuserToReturn;
	private RunningDiffuser rdToReturn;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		diffuserName = "test";
		teamId = 1;
		diffuserToReturn = new Diffuser(diffuserName);
		rdToReturn = new RunningDiffuser(diffuserToReturn);
	}

	// Creation
	@Test
	public void testCreate() {
		when(mockDiffuserRepository.add(any(Diffuser.class))).thenReturn(diffuserToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		Diffuser newDiffuser = new Diffuser(diffuserName);

		assertEquals(diffuserToReturn, service.create(newDiffuser));
	}


	// Update
	@Test
	public void testUpdate() {
		when(mockDiffuserRepository.update(any(Diffuser.class))).thenReturn(diffuserToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		Diffuser newDiffuser = new Diffuser(diffuserName);

		assertEquals(diffuserToReturn, service.update(newDiffuser));
	}

	// Getters
	@Test
	public void testGetDiffuserByName() {
		when(mockDiffuserRepository.getByName(diffuserName)).thenReturn(diffuserToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertEquals(diffuserName, service.getDiffuserByName(diffuserName).getName());
	}

	@Test
	public void testGetDiffuserByNameFail() {
		when(mockDiffuserRepository.getByName(diffuserName)).thenReturn(diffuserToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertNull(service.getDiffuserByName("WrongName"));
	}

	@Test
	public void testGetRunningDiffuserByDiffuserId() {
		when(mockRunningDiffuserRepository.getByDiffuserId(diffuserId)).thenReturn(rdToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertEquals(rdToReturn, service.getRunningDiffuserByDiffuserId(diffuserId));
	}

	@Test
	public void testGetRunningDiffuserByDiffuserIdFail() {
		when(mockRunningDiffuserRepository.getByDiffuserId(diffuserId)).thenReturn(rdToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertNull(service.getRunningDiffuserByDiffuserId(43));
	}

	@Test
	public void testGetScreenIdsOfRunningDiffuser() {
		List<Integer> screenIds = new ArrayList<>();
		screenIds.add(42);
		when(mockRunningDiffuserRepository.getScreenIdsOfRunningDiffuser(diffuserId)).thenReturn(screenIds);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertEquals(42, (int) service.getScreenIdsOfRunningDiffuserById(diffuserId).get(0));
	}

	@Test
	public void testGetAllDiffusers() {

		List<Diffuser> diffusers = new ArrayList<>();
		diffusers.add(new Diffuser(diffuserName));

		List<DiffuserData> diffuserData = new ArrayList<>();
		diffuserData.add(new DiffuserData(diffusers.get(0)));

		when(mockDiffuserRepository.getAll()).thenReturn(diffusers);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertEquals(diffuserData.get(0).getName(), service.getAllDiffusers().get(0).getName());
	}

	@Test
	public void testGetAllDiffusersOfTeam() {
		List<Integer> diffuserIds = new ArrayList<>();
		int diffuserId = 42;
		diffuserIds.add(diffuserId);

		List<DiffuserData> diffuserData = new ArrayList<>();
		diffuserData.add(new DiffuserData(diffuserName));

		when(mockDiffuserRepository.getAllDiffuserIdsOfTeam(teamId)).thenReturn(diffuserIds);
		when(mockDiffuserRepository.getById(any(Integer.class))).thenReturn(new Diffuser(diffuserName));
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertEquals(diffuserData.get(0).getName(), service.getAllDiffusersOfTeam(teamId).get(0).getName());
	}

	@Test
	public void testGetAllActiveSchedulesOfTeam() {
		List<Integer> diffuserIds = new ArrayList<>();
		int diffuserId = 42;
		int activeDiffuserId = 43;
		diffuserIds.add(diffuserId);
		diffuserIds.add(activeDiffuserId);

		String activeName = "active";
		Diffuser activeDiffuser = new Diffuser(activeName);
		activeDiffuser.setId(activeDiffuserId);

		when(mockDiffuserRepository.getAllDiffuserIdsOfTeam(any(Integer.class))).thenReturn(diffuserIds);
		when(mockDiffuserRepository.getByName(diffuserName)).thenReturn(new Diffuser(diffuserName));
		when(mockDiffuserRepository.getById(diffuserId)).thenReturn(new Diffuser(diffuserName));
		when(mockDiffuserRepository.getByName(activeName)).thenReturn(activeDiffuser);
		when(mockDiffuserRepository.getById(activeDiffuserId)).thenReturn(activeDiffuser);
		when(mockRunningDiffuserRepository.getByDiffuserId(any(Integer.class))).thenReturn(new RunningDiffuser(activeDiffuser));
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertEquals(activeDiffuser.getName(), service.getAllActiveDiffusersOfTeam(teamId).get(0).getName());
	}
}
