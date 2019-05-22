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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ScheduleUnitTest {

	@Mock
	private ScheduleRepository mockScheduleRepository;

	@Mock
	private RunningScheduleRepository mockRunningScheduleRepository;

	private String scheduleName;
	private int teamId;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		scheduleName = "test";
		teamId = 1;
	}


	@Test
	public void testCreateSchedule() {
		Schedule scheduleToReturn = new Schedule(scheduleName);
		when(mockScheduleRepository.add(any(Schedule.class))).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		Schedule newSchedule = new Schedule(scheduleName);

		assertEquals(scheduleToReturn.getName(), service.create(newSchedule).getName());
	}

	@Test
	public void testUpdateSchedule() {
		Schedule scheduleToReturn = new Schedule(scheduleName);
		when(mockScheduleRepository.update(any(Schedule.class))).thenReturn(scheduleToReturn);
		ScheduleService service = new ScheduleService(mockScheduleRepository, mockRunningScheduleRepository);

		Schedule newSchedule = new Schedule(scheduleName);

		assertEquals(scheduleToReturn.getName(), service.update(newSchedule).getName());
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
