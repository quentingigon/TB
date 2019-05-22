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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DiffuserUnitTest {

	@Mock
	private DiffuserRepository mockDiffuserRepository;

	@Mock
	private RunningDiffuserRepository mockRunningDiffuserRepository;

	private String name;
	private int teamId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		name = "test";
		teamId = 1;
	}

	@Test
	public void testCreate() {
		Diffuser diffuserToReturn = new Diffuser(name);
		when(mockDiffuserRepository.add(any(Diffuser.class))).thenReturn(diffuserToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		Diffuser newDiffuser = new Diffuser(name);

		assertEquals(diffuserToReturn, service.create(newDiffuser));
	}

	@Test
	public void testUpdate() {
		Diffuser diffuserToReturn = new Diffuser(name);
		when(mockDiffuserRepository.update(any(Diffuser.class))).thenReturn(diffuserToReturn);
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		Diffuser newDiffuser = new Diffuser(name);

		assertEquals(diffuserToReturn, service.update(newDiffuser));
	}

	@Test
	public void testGetAllDiffusers() {

		List<Diffuser> diffusers = new ArrayList<>();
		diffusers.add(new Diffuser(name));

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
		diffuserData.add(new DiffuserData(name));

		when(mockDiffuserRepository.getAllDiffuserIdsOfTeam(teamId)).thenReturn(diffuserIds);
		when(mockDiffuserRepository.getById(any(Integer.class))).thenReturn(new Diffuser(name));
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
		when(mockDiffuserRepository.getByName(name)).thenReturn(new Diffuser(name));
		when(mockDiffuserRepository.getById(diffuserId)).thenReturn(new Diffuser(name));
		when(mockDiffuserRepository.getByName(activeName)).thenReturn(activeDiffuser);
		when(mockDiffuserRepository.getById(activeDiffuserId)).thenReturn(activeDiffuser);
		when(mockRunningDiffuserRepository.getByDiffuserId(any(Integer.class))).thenReturn(new RunningDiffuser(activeDiffuser));
		DiffuserService service = new DiffuserService(mockDiffuserRepository, mockRunningDiffuserRepository);

		assertEquals(activeDiffuser.getName(), service.getAllActiveDiffusersOfTeam(teamId).get(0).getName());
	}
}
