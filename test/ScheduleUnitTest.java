import models.db.RunningSchedule;
import models.db.Schedule;
import models.entities.ScheduleData;
import models.repositories.interfaces.RunningScheduleRepository;
import models.repositories.interfaces.ScheduleRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import services.ScheduleService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ScheduleUnitTest {

	@Mock
	private ScheduleRepository mockScheduleRepository;

	@Mock
	private RunningScheduleRepository mockRunningScheduleRepository;

	private RunningSchedule runningScheduleToReturn;
	private Schedule scheduleToReturn;
	private String scheduleName;
	private int teamId;
	private int scheduleId;
	private int runningScheduleId;
	private int screenId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		scheduleName = "test";
		teamId = 1;
		scheduleId = 42;
		screenId = 100;
		runningScheduleId = scheduleId;
		scheduleToReturn = new Schedule(scheduleName);
		runningScheduleToReturn = new RunningSchedule(scheduleToReturn);
	}

	// Creation
	@Test
	public void testCreateSchedule() {
		when(mockScheduleRepository.add(any(Schedule.class))).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		Schedule newSchedule = new Schedule(scheduleName);

		assertEquals(scheduleToReturn.getName(), service.create(newSchedule).getName());
	}

	@Test
	public void testCreateRunningSchedule() {
		Schedule schedule = new Schedule(scheduleName);
		int scheduleId = 1;
		schedule.setId(scheduleId);
		RunningSchedule runningScheduleToReturn = new RunningSchedule(schedule);
		when(mockRunningScheduleRepository.add(any(RunningSchedule.class))).thenReturn(runningScheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		RunningSchedule newRunningSchedule = new RunningSchedule(schedule);

		assertEquals(runningScheduleToReturn.getScheduleId(), service.create(newRunningSchedule).getScheduleId());
	}

	// Update
	@Test
	public void testUpdateSchedule() {
		when(mockScheduleRepository.update(any(Schedule.class))).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		Schedule newSchedule = new Schedule(scheduleName);

		assertEquals(scheduleToReturn.getName(), service.update(newSchedule).getName());
	}

	@Test
	public void testUpdateRunningSchedule() {
		Schedule schedule = new Schedule(scheduleName);
		int scheduleId = 1;
		schedule.setId(scheduleId);
		RunningSchedule runningScheduleToReturn = new RunningSchedule(schedule);
		when(mockRunningScheduleRepository.update(any(RunningSchedule.class))).thenReturn(runningScheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		RunningSchedule newRunningSchedule = new RunningSchedule(schedule);

		assertEquals(runningScheduleToReturn.getScheduleId(), service.update(newRunningSchedule).getScheduleId());
	}

	// Getters

	@Test
	public void testGetScheduleById() {
		when(mockScheduleRepository.getById(scheduleId)).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(scheduleName, service.getScheduleById(scheduleId).getName());
	}

	@Test
	public void testGetScheduleByIdFail() {
		when(mockScheduleRepository.getById(scheduleId)).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertNull(service.getScheduleById(43));
	}

	@Test
	public void testGetScheduleByName() {
		when(mockScheduleRepository.getByName(scheduleName)).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(scheduleName, service.getScheduleByName(scheduleName).getName());
	}

	@Test
	public void testGetScheduleByNameFail() {
		when(mockScheduleRepository.getByName(scheduleName)).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertNull(service.getScheduleByName("WrongName"));
	}

	@Test
	public void testGetRunningScheduleById() {
		when(mockRunningScheduleRepository.getById(runningScheduleId)).thenReturn(runningScheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(runningScheduleToReturn, service.getRunningScheduleById(runningScheduleId));
	}

	@Test
	public void testGetRunningScheduleByIdFail() {
		when(mockRunningScheduleRepository.getById(runningScheduleId)).thenReturn(runningScheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertNull(service.getRunningScheduleById(43));
	}

	@Test
	public void testGetRunningScheduleByScheduleId() {
		when(mockRunningScheduleRepository.getByScheduleId(scheduleId)).thenReturn(runningScheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(runningScheduleToReturn, service.getRunningScheduleByScheduleId(scheduleId));
	}

	@Test
	public void testGetRunningScheduleByScheduleIdFail() {
		when(mockRunningScheduleRepository.getByScheduleId(scheduleId)).thenReturn(runningScheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertNull(service.getRunningScheduleByScheduleId(43));
	}

	@Test
	public void testGetRunningScheduleOfScreen() {

		when(mockRunningScheduleRepository.getRunningScheduleIdByScreenId(screenId)).thenReturn(runningScheduleId);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(runningScheduleId, (int) service.getRunningScheduleOfScreenById(screenId));
	}

	@Test
	public void testGetAllSchedules() {

		List<Schedule> schedules = new ArrayList<>();
		schedules.add(new Schedule(scheduleName));

		List<ScheduleData> scheduleData = new ArrayList<>();
		scheduleData.add(new ScheduleData(schedules.get(0)));

		when(mockScheduleRepository.getAll()).thenReturn(schedules);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(scheduleData.get(0).getName(), service.getAllSchedules().get(0).getName());
	}

	@Test
	public void testGetAllSchedulesOfTeam() {
		List<Integer> scheduleIds = new ArrayList<>();
		int scheduleId = 42;
		scheduleIds.add(scheduleId);

		List<ScheduleData> scheduleData = new ArrayList<>();
		scheduleData.add(new ScheduleData(scheduleName));

		when(mockScheduleRepository.getAllScheduleIdsOfTeam(teamId)).thenReturn(scheduleIds);
		when(mockScheduleRepository.getById(any(Integer.class))).thenReturn(new Schedule(scheduleName));
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(scheduleData.get(0).getName(), service.getAllSchedulesOfTeam(teamId).get(0).getName());
	}

	@Test
	public void testGetAllActiveSchedulesOfTeam() {
		List<Integer> scheduleIds = new ArrayList<>();
		int scheduleId = 42;
		scheduleIds.add(scheduleId);

		Schedule schedule = new Schedule(scheduleName);
		schedule.setId(scheduleId);

		List<ScheduleData> scheduleData = new ArrayList<>();
		scheduleData.add(new ScheduleData(scheduleName));

		when(mockScheduleRepository.getAllScheduleIdsOfTeam(any(Integer.class))).thenReturn(scheduleIds);
		when(mockScheduleRepository.getByName(any(String.class))).thenReturn(schedule);
		when(mockScheduleRepository.getById(any(Integer.class))).thenReturn(schedule);
		when(mockRunningScheduleRepository.getByScheduleId(any(Integer.class))).thenReturn(new RunningSchedule(schedule));
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		assertEquals(scheduleData.get(0).getName(), service.getAllActiveSchedulesOfTeam(teamId).get(0).getName());
	}
}
