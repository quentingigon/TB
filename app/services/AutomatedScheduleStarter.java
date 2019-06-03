package services;

import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.Screen;
import models.repositories.interfaces.FluxRepository;
import models.repositories.interfaces.RunningScheduleRepository;
import models.repositories.interfaces.ScheduleRepository;
import models.repositories.interfaces.ScreenRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is called when the application starts to recreate a
 * RunningScheduleThread from the RunningSchedules in the database.
 */
@Singleton
public class AutomatedScheduleStarter {

    private final RunningScheduleThreadManager threadManager;
    private final FluxManager fluxManager;
    private final TimeTableUtils timeTableUtils;

    private final ScreenRepository screenRepository;
    private final FluxRepository fluxRepository;
    private final ScheduleRepository scheduleRepository;
    private final RunningScheduleRepository runningScheduleRepository;
    private final FluxChecker fluxChecker;
    private final ServicePicker servicePicker;

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("application");

    @Inject
    public AutomatedScheduleStarter(FluxManager fluxManager,
                                    TimeTableUtils timeTableUtils,
                                    ScreenRepository screenRepository,
                                    FluxRepository fluxRepository,
                                    RunningScheduleRepository repo,
                                    ScheduleRepository scheduleRepository,
                                    RunningScheduleThreadManager threadManager,
                                    FluxChecker fluxChecker,
                                    ServicePicker servicePicker) {
        this.servicePicker = servicePicker;
        this.fluxChecker = fluxChecker;
        this.screenRepository = screenRepository;
        this.fluxRepository = fluxRepository;
        this.scheduleRepository = scheduleRepository;
        this.fluxManager = fluxManager;
        this.threadManager = threadManager;
        this.runningScheduleRepository = repo;
        this.timeTableUtils = timeTableUtils;

        // This code is called when the application starts.
        // get all existing runningSchedules
        for (RunningSchedule rs: runningScheduleRepository.getAll()) {

            Schedule schedule = scheduleRepository.getById(rs.getScheduleId());

            if (schedule != null) {

                List<Screen> screens = new ArrayList<>();
                for (Integer screenId : runningScheduleRepository.getScreensIdsByRunningScheduleId(rs.getId())) {
                    Screen screen = screenRepository.getById(screenId);
                    if (screen != null)
                        screens.add(screen);
                }

                RunningScheduleThread service = new RunningScheduleThread(
                    rs,
                    screens,
                    new ArrayList<>(schedule.getFallbacks()),
                    timeTableUtils.getTimeTable(schedule),
                    servicePicker,
                    fluxChecker,
                    schedule.isKeepOrder());

                service.addObserver(fluxManager);

                // the schedule is activated
                threadManager.addRunningScheduleThread(schedule.getId(), service);
            }
        }
    }
}
