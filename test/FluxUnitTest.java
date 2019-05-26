import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.db.ScheduledFlux;
import models.entities.FluxData;
import models.repositories.interfaces.FluxRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.FluxService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FluxUnitTest {

	@Mock
	private FluxRepository mockFluxRepository;

	private String fluxName;
	private String fluxUrl;
	private int fluxId;
	private int teamId;
	private Flux fluxToReturn;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		fluxName = "test";
		fluxUrl = "testUrl";
		teamId = 1;
		fluxId = 42;
		fluxToReturn = new Flux(fluxName, fluxUrl);
	}

	// Creation
	@Test
	public void testCreateFlux() {
		when(mockFluxRepository.addFlux(any(Flux.class))).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		Flux newFlux = new Flux(fluxName, fluxUrl);

		assertEquals(fluxToReturn.getName(), service.create(newFlux).getName());
	}

	@Test
	public void testCreateFluxWithExistingName() {
		Flux newFlux = new Flux(fluxName, fluxUrl);
		when(mockFluxRepository.addFlux(newFlux)).thenReturn(null);
		FluxService service = new FluxService(mockFluxRepository);

		assertNull(service.create(newFlux));
	}

	@Test
	public void testCreateGeneralFlux() {
		GeneralFlux fluxToReturn = new GeneralFlux(1);
		when(mockFluxRepository.addGeneralFlux(any(GeneralFlux.class))).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		GeneralFlux newFlux = new GeneralFlux(1);

		assertEquals(fluxToReturn.getFluxId(), service.createGeneral(newFlux).getFluxId());
	}

	@Test
	public void testCreateScheduledFlux() {
		ScheduledFlux fluxToReturn = new ScheduledFlux(1, 1, 1);
		when(mockFluxRepository.addScheduledFlux(any(ScheduledFlux.class))).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		ScheduledFlux newFlux = new ScheduledFlux(1, 1, 1);

		assertEquals(fluxToReturn.getFluxId(), service.createScheduled(newFlux).getFluxId());
	}

	@Test
	public void testCreateLocatedFlux() {
		LocatedFlux fluxToReturn = new LocatedFlux(1, 1);
		when(mockFluxRepository.addLocatedFlux(any(LocatedFlux.class))).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		LocatedFlux newFlux = new LocatedFlux(1, 1);

		assertEquals(fluxToReturn.getFluxId(), service.createLocated(newFlux).getFluxId());
	}


	// Update
	@Test
	public void testUpdateFlux() {
		when(mockFluxRepository.update(any(Flux.class))).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		Flux newFlux = new Flux(fluxName, fluxUrl);

		assertEquals(fluxToReturn.getName(), service.update(newFlux).getName());
	}


	// Getters
	@Test
	public void testGetFluxByName() {
		when(mockFluxRepository.getByName(fluxName)).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		assertEquals(fluxName, service.getFluxByName(fluxName).getName());
	}

	@Test
	public void testGetFluxByNameFail() {
		when(mockFluxRepository.getByName(fluxName)).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		assertNull(service.getFluxByName("WrongName"));
	}


	@Test
	public void testGetFluxById() {
		when(mockFluxRepository.getById(fluxId)).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		assertEquals(fluxName, service.getFluxById(fluxId).getName());
	}

	@Test
	public void testGetFluxByIdFail() {
		when(mockFluxRepository.getById(fluxId)).thenReturn(fluxToReturn);
		FluxService service = new FluxService(mockFluxRepository);

		assertNull(service.getFluxById(43));
	}

	@Test
	public void testGetAllFluxes() {

		List<Flux> fluxes = new ArrayList<>();
		fluxes.add(new Flux(fluxName, fluxUrl));

		List<FluxData> fluxData = new ArrayList<>();
		fluxData.add(new FluxData(fluxes.get(0)));

		when(mockFluxRepository.getAll()).thenReturn(fluxes);
		FluxService service = new FluxService(mockFluxRepository);

		assertEquals(fluxData.get(0).getName(), service.getAllFluxes().get(0).getName());
	}

	@Test
	public void testGetAllFluxesOfTeam() {
		List<Integer> fluxIds = new ArrayList<>();
		int fluxId = 42;
		fluxIds.add(fluxId);


		List<FluxData> fluxesData = new ArrayList<>();
		fluxesData.add(new FluxData(fluxName, fluxUrl));

		when(mockFluxRepository.getAllFluxIdsOfTeam(teamId)).thenReturn(fluxIds);
		when(mockFluxRepository.getById(any(Integer.class))).thenReturn(new Flux(fluxName, fluxUrl));
		FluxService service = new FluxService(mockFluxRepository);

		assertEquals(fluxesData.get(0).getName(), service.getAllFluxesOfTeam(teamId).get(0).getName());
	}


	@Test
	public void testGetAllLocatedFluxesOfTeam() {
		List<Integer> fluxIds = new ArrayList<>();
		int fluxId = 42;
		int siteId = 1;
		fluxIds.add(fluxId);


		List<FluxData> fluxesData = new ArrayList<>();
		fluxesData.add(new FluxData(fluxName, fluxUrl));

		when(mockFluxRepository.getAllFluxIdsOfTeam(teamId)).thenReturn(fluxIds);
		when(mockFluxRepository.getById(any(Integer.class))).thenReturn(new Flux(fluxName, fluxUrl));
		when(mockFluxRepository.getLocatedFluxByFluxId(any(Integer.class))).thenReturn(new LocatedFlux(fluxId, siteId));
		FluxService service = new FluxService(mockFluxRepository);

		assertEquals(fluxesData.get(0).getName(), service.getAllLocatedFluxesOfTeam(teamId).get(0).getName());
	}

	@Test
	public void testGetAllGeneralFluxesOfTeam() {
		List<Integer> fluxIds = new ArrayList<>();
		int fluxId = 42;
		fluxIds.add(fluxId);


		List<FluxData> fluxesData = new ArrayList<>();
		fluxesData.add(new FluxData(fluxName, fluxUrl));

		when(mockFluxRepository.getAllFluxIdsOfTeam(teamId)).thenReturn(fluxIds);
		when(mockFluxRepository.getById(any(Integer.class))).thenReturn(new Flux(fluxName, fluxUrl));
		when(mockFluxRepository.getGeneralFluxByFluxId(any(Integer.class))).thenReturn(new GeneralFlux(fluxId));
		FluxService service = new FluxService(mockFluxRepository);

		assertEquals(fluxesData.get(0).getName(), service.getAllGeneralFluxesOfTeam(teamId).get(0).getName());
	}
}
